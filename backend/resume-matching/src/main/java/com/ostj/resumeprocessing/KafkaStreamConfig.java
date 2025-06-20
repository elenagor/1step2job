package com.ostj.resumeprocessing;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ostj.OpenAIProvider;
import com.ostj.dataaccess.PositionReceiver;
import com.ostj.dataaccess.PersonReceiver;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.MatchResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.Position;
import com.ostj.entities.MatchResult;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.StrictEnumTypeAdapterFactory;
import com.ostj.utils.Utils;
import org.jsoup.Jsoup;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig  {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();

    @Autowired
    ConfigProvider configProvider;

    @Value(value = "${ostj.kstream.topic}")
    String topic_name;

    @Value(value = "${spring.application.name}")
    String appName;

    @Value(value = "${ostj.kstream.topic.output}")
    String outputTopic;

    @Value(value = "${ostj.match.retry}")
    int retry_callai;

    @Autowired
	OpenAIProvider resumeMatcher;

    @Autowired
	SQLAccess dbConnector;

    @Autowired
	PromptManager promptManager;

    @Autowired
	PersonReceiver personReceiver;

    @Autowired
	PositionReceiver positionReceiver;

    @Autowired
	MatchResultManager resultManager;

    @Autowired
    Serde<ResumeProcessEvent> resumeProcessEvent;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, configProvider.getBootstrapAddress());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        log.debug("kStreamsConfig: appName={}, bootstrapAddress={}, topic_name={}", appName, configProvider.getBootstrapAddress(), topic_name);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String,String> kStream(StreamsBuilder kStreamBuilder){

        KStream<String, String> stream = kStreamBuilder.stream(topic_name);
        stream.peek((key, value) -> { log.debug("RECEIVE key={}, value={}", key, value);})
        .mapValues(v -> mapStringValueToEventRecord(v))
        .filter((key, value) -> value != null )
        .peek((key, value) -> processMessage(key, value))
        .filter((key, value) ->  value.isFinished )
        .peek((key, value) -> { log.debug("SEND key={}, value={}", key, value);})
        .to(outputTopic, Produced.with( Serdes.String(), resumeProcessEvent ))
        ;
        return stream;
    }

    private ResumeProcessEvent mapStringValueToEventRecord(String value) {
		log.trace("Message String Value {}", value);
		try {
            JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
			return gson.fromJson(jsonValue, ResumeProcessEvent.class);
		} catch (Throwable e) {
			log.error("Error parsing json message {}", e);
		}
		return null;
	}

    public void processMessage(String key, ResumeProcessEvent record) {
        log.debug("Start process key={}, value={}", key, record);
        try{
            if(!record.isFinished){
                String  prompt = promptManager.getPrompt(record);
                if(prompt == null){
                    throw new Exception(  String.format("There is no prompt with id=%d or filename=%s", record.PromptId, record.promptFilePath));
                }
                Person person = personReceiver.getPersonData(record);
                if(person == null){
                    throw new Exception(  String.format("There is no person with id=%d or filename=%s", record.PersonId, record.resumeFilePath));
                }
                processPersonMessage(record, person, prompt);
            }
        }
        catch(Throwable e){
            log.error("Error: " + e.toString());
        }
        log.debug("Finish process key={}, value={}", key, record);
    }

    private void processPersonMessage(ResumeProcessEvent record, Person person, String  prompt) throws Exception{
        log.info("Start Process Person: {}", person.toString());
        for(Profile profile : person.profiles){
            log.debug("Process person profile with titles count: {}", profile.job_titles.size());
            Position position = positionReceiver.getPosition(record);
            log.debug("Found Posion: {}", position);
            processResume( prompt,  person,  profile,  position);
        }
    }
    private void processResume(String prompt, Person person, Profile profile, Position position){
        log.debug("Start process resume person={}, profileId={}, positionId={}",person.id, profile.id, position.id);
        try{
            MatchResult result = null;
            for(int step = 1; step <= retry_callai; step++){
                String response = call_openai(prompt, profile, position ) ;
                result = createMatchResultFromOpenAiResponse(person, profile, position, response);
                if(result == null){
                    log.debug("problem to process person={} - retry step={}", person, step);
                }
                else{
                    break;
                }
            } 
            if(result != null){
                result.Id = resultManager.updateMatchResult(result);
                log.debug("Updated match result to DB {}", result);
            }
        }
        catch(Exception e){
            log.error("Error on procces profileId={} jobId={} {}", profile.id, position.id, e);
        }
    }

    private MatchResult createMatchResultFromOpenAiResponse(Person person, Profile profile, Position position, String response){
        try{
            String jsonString = Utils.getJsonContextAsString(response);
            log.trace("Json Response: {}", jsonString);
            JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
            log.trace("Json Responce: {}", jsonValue);
            MatchResult result = gson.fromJson(jsonValue, MatchResult .class);
            log.debug("Matched result SCORE={}, {}", result.overall_score, result.score_explanation);
            result.Person_Id = person.id;
            result.Profile_Id = profile.id;
            result.Position_Id = position.id;
            result.date = new java.sql.Timestamp(System.currentTimeMillis()); // Current date
            result.Reasoning = Utils.getThinksAsText(response);
            log.trace("Created MatchResult: {}", result);
            return result;
        }
        catch(Exception e){
            log.error("Error parsing AI responce", profile.id, position.id, e);
        }
        return null;
    }

    private String call_openai(String  prompt, Profile profile, Position position) throws Exception{
        log.debug("Start call openai for profileId={}, positionId={}", profile.id, position.id);
        if(prompt == null)
            throw new MissingArgumentException("prompt");
        if(profile.resume == null)
            throw new MissingArgumentException("profile.resume");
        if(position.description == null)
            throw new MissingArgumentException("job.description");

        // Convert HTML to plain text
        String position_description_plan_text = Jsoup.parse(position.description).text();
        log.trace(position_description_plan_text);

        String response = resumeMatcher.call_openai( profile.resume, position_description_plan_text, prompt) ;
        log.trace("AI Response: {}", response);

        return response;
    }
}
