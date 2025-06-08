package com.ostj.resumeprocessing;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
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
import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.MatchResult;
import com.ostj.entities.Position;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;

import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.EmailSender;
import com.ostj.utils.StrictEnumTypeAdapterFactory;
import com.ostj.utils.Utils;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig  {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();
    private int overall_score_treshhold;
    @Autowired
    ConfigProvider configProvider;

    @Value(value = "${ostj.kstream.topic}")
    String topic_name;

    @Value(value = "${spring.application.name}")
    String appName;

    @Value(value = "${ostj.email.sender}")
    String emailSenderAddress;

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
	ResultManager resultManager;

    @Autowired
    EmailSender emailSender;

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
        overall_score_treshhold = Integer.parseInt( configProvider.getProperty("MATCH_TRESHHOLD", "5"));
        log.debug("Start overall_score_treshhold={}", overall_score_treshhold);

        KStream<String, String> stream = kStreamBuilder.stream(topic_name);
        stream.mapValues(value -> mapStringValueToEventRecord(value))
        .filter((key, value) -> value != null)
        .peek((key, value) -> processMessage(key, value))
        ;
        return stream;
    }

    private ResumeProcessEvent mapStringValueToEventRecord(String value) {
        log.info("Start mapping string to record: value={}",value);
        
        overall_score_treshhold = Integer.parseInt( configProvider.getProperty("MATCH_TRESHHOLD", "5"));
        log.debug("Current overall_score_treshhold={}", overall_score_treshhold);
		
        ResumeProcessEvent record = null;
		try {
            JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
			record = gson.fromJson(jsonValue, ResumeProcessEvent.class);
            log.debug("Message mapped Value: {}", record.toString());
		} catch (Throwable e) {
			log.error("Error parsing json message {}", e);
		}
		return record;
	}

    public void processMessage(String key, ResumeProcessEvent record) {
        log.debug("Start process message key={}, value={}", key, record);
        try{
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
        catch(Throwable e){
            log.error("Error: " + e.toString());
        }
    }

    private void  processPersonMessage(ResumeProcessEvent record, Person person, String  prompt) throws Exception{
        log.info("Start Process Person: {}", person.toString());
        for(Profile profile : person.profiles){
            log.debug("Process person profile with titles count: {}", profile.job_titles.size());
            Position position = positionReceiver.getPosition(record);
            log.debug("Found Posion: {}", position);
            processResume( prompt,  person,  profile,  position);
        }
    }
    private void processResume(String prompt, Person person, Profile profile, Position position){
        try{
            String response = call_openai(prompt, profile, position ) ;
            MatchResult result = createMatchResultFromOpenAiResponse(person, profile, position, response);
            if(result != null){
                result.Id = resultManager.saveMatchResult(result);
                log.info("Saved match result to DB {}", result);
                String emailBody = resultManager.createEmailBody(result, person, position);
                emailSender.withTO(person.email).withBody(emailBody).withSubject("1Step2Job found a job for you").send(emailSenderAddress);
            }
        }
        catch(Exception e){
            log.error("Error on proces profileId={} jobId={} {}", profile.id, position.id, e);
        }
    }

    private MatchResult createMatchResultFromOpenAiResponse(Person person, Profile profile, Position position, String response){
        String jsonString = Utils.getJsonContextAsString(response);
        log.trace("Json Response: {}", jsonString);
        JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
        log.trace("Json Responce: {}", jsonValue);
        MatchResult result = gson.fromJson(jsonValue, MatchResult .class);
        log.debug("Matched result overall_score={}, {}", result.overall_score, result.score_explanation);
        if(result.overall_score >= overall_score_treshhold){
            result.Person_Id = person.id;
            result.Profile_Id = profile.id;
            result.Position_Id = position.id;
            result.date = new java.util.Date(); // Current date
            result.Reasoning = Utils.getThinksAsText(response);
            log.trace("Created MatchResult: {}", result);
            return result;
        }
        return null;
    }

    private String call_openai(String  prompt, Profile profile, Position position) throws Exception{
        if(prompt == null)
            throw new MissingArgumentException("prompt");
        if(profile.resume == null)
            throw new MissingArgumentException("profile.resume");
        if(position.description == null)
            throw new MissingArgumentException("job.description");

        log.debug("Start call openai for profileId={}, positionId={}", profile.id, position.id);
        String response = resumeMatcher.call_openai( profile.resume, position.description, prompt) ;

        log.trace("AI Response: {}", response);
        return response;
    }


}
