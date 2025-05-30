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
import com.ostj.dataaccess.JobsReceiver;
import com.ostj.dataaccess.PersonReceiver;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Result;
import com.ostj.entities.Job;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.openai.AIMatcher;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.StrictEnumTypeAdapterFactory;
import com.ostj.utils.Utils;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig  {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();
    
    @Value(value = "${ostj.kstream.topic}")
    String topic_name;

    @Value(value = "${spring.kafka.bootstrap-servers}")
    String bootstrapAddress;

    @Value(value = "${spring.application.name}")
    String appName;;

    @Value(value = "${ostj.match.treshhold}")
    int match_treshhold;

    @Autowired
	AIMatcher resumeMatcher;

    @Autowired
	SQLAccess dbConnector;

    @Autowired
	PromptManager promptManager;

    @Autowired
	PersonReceiver personManager;

    @Autowired
	JobsReceiver jobManager;

    @Autowired
	ResultManager resultManager;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        log.trace("kStreamsConfig: appName={}, bootstrapAddress={}, topic_name={}", appName, bootstrapAddress, topic_name);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String,String> kStream(StreamsBuilder kStreamBuilder){
        KStream<String, String> stream = kStreamBuilder.stream(topic_name);
        stream.mapValues(value -> mapStringValueToEventRecord(value))
        .filter((key, value) -> value != null)
        .peek((key, value) -> processMessage(key, value))
        ;
        return stream;
    }

    private ResumeProcessEvent mapStringValueToEventRecord(String value) {
        log.info("\nvalue={}",value);
		ResumeProcessEvent record = null;
		try {
            JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
			record = gson.fromJson(jsonValue, ResumeProcessEvent.class);
            log.trace("Message mapped Value: {}", record.toString());
		} catch (Throwable e) {
			log.error("Error parsing json message {}", e);
		}
		return record;
	}

    public void processMessage(String key, ResumeProcessEvent record) {
        log.debug("key={}, value={}", key, record);
        try{
            String  prompt = promptManager.getPrompt(record);
            if(prompt == null){
                throw new Exception(  String.format("There is no prompt with id=%d or filename=%s", record.PromptId, record.promptFilePath));
            }
            Person person = personManager.getPersonData(record);
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
        log.info("Processed person: {}", person.toString());
        for(Profile profile : person.profiles){
            log.debug("Processed title: {}", profile.title);
            Job job = jobManager.getJob(record);
            log.debug("Found Job: {}", job);
            processResume( prompt,  person,  profile,  job);
        }
    }
    private void processResume(String prompt, Person person, Profile profile, Job job){
        try{
            String response = call_openai(prompt, profile, job ) ;
            Result result = createMatchResultFromOpenAiResponse(person, profile, job, response);
            result.Id = resultManager.saveMatchResult(result);
            log.debug("Saved result to DB {}", result);
            log.info("Saved resultId: {} ", result.Id);
        }
        catch(Exception e){
            log.error("Error on proces profileId={} jobId={} {}", profile.id, job.id, e);
        }
    }

    private Result createMatchResultFromOpenAiResponse(Person person, Profile profile, Job job, String response){
        String jsonString = Utils.getJsonContextAsString(response);
        log.trace("Json Response: {}", jsonString);
        JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
        log.trace("Json Responce: {}", jsonValue);
        Result result = gson.fromJson(jsonValue, Result .class);
        result.PersonId = person.id;
        result.ProfileId = profile.id;
        result.JobId = job.id;
        result.date = new java.util.Date(); // Current date
        result.Reasoning = Utils.getThinksAsText(response);
        log.debug("Result: {}", result);
        return result;
    }

    private String call_openai(String  prompt, Profile profile, Job job) throws Exception{
        if(prompt == null)
            throw new MissingArgumentException("prompt");
        if(profile.resume == null)
            throw new MissingArgumentException("profile.resume");
        if(job.description == null)
            throw new MissingArgumentException("job.description");

        String response = resumeMatcher.call_openai( profile.resume, job.description, prompt) ;

        log.trace("AI Response: {}", response);
        return response;
    }


}
