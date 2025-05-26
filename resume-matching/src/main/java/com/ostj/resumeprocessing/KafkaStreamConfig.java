package com.ostj.resumeprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.ostj.dataaccess.JobManager;
import com.ostj.dataaccess.PersonManager;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Job;
import com.ostj.dataentity.Result;
import com.ostj.dataentity.Person;
import com.ostj.dataentity.Profile;
import com.ostj.openai.AIMatcher;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.StrictEnumTypeAdapterFactory;

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
	PersonManager personManager;

    @Autowired
	JobManager jobManager;

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
            processPersonMessage(record);
        }
        catch(Throwable e){
            log.error("Error: " + e.toString());
        }
    }

    private void  processPersonMessage(ResumeProcessEvent record) throws Exception{
        String  prompt = promptManager.getPrompt(record);
        if(prompt == null){
            throw new Exception(  String.format("There is no prompt with id=%d", record.PromptId));
        }
        Person person = personManager.getPersonData(record);
        if(person == null){
            throw new Exception(  String.format("There is no person with id=%d", record.PersonId));
        }
        log.debug("Processed person: {}", person.toString());
        for(Profile profile : person.resumes){
            log.debug("Processed title: {}", profile.Title);
            Job job = jobManager.getJob(record);
            log.debug("Found Job: {}", job);
            int savedMatchResults = processResume( prompt,  person,  profile,  job);
            log.debug("Saved resultId: {} ", savedMatchResults);
        }
    }
    private int processResume(String prompt, Person person, Profile resume, Job job){
        try{
            String response = call_openai(prompt, resume, job ) ;
            
            Result result = convertResponce(person, resume, job, response);

            result.Id = dbConnector.saveMatchResult(result);
            log.debug("Saved result to DB {}", result);
            return result.Id;
        }
        catch(Exception e){
            log.error("Error on proces resumeId={} jobId={} {}", resume.Id, job.Id, e);
        }
        return -1;
    }

    private Result  convertResponce(Person person, Profile resume, Job job, String response){
        log.trace("AI Responce: {}", response);
        JsonObject jsonValue = JsonParser.parseString(response).getAsJsonObject();
        log.trace("Json Responce: {}", jsonValue);
        Result  result = gson.fromJson(jsonValue, Result .class);
        result.PersonId = person.Id;
        result.ResumeId = resume.Id;
        result.JobId = job.Id;
        log.debug("Result: {}", result);
        return result;
    }

    private String call_openai(String  prompt, Profile profile, Job job) throws Exception{
        if(prompt == null)
            throw new MissingArgumentException("record.prompt");
        if(profile == null)
            throw new MissingArgumentException("profile.content");
        if(job == null)
            throw new MissingArgumentException("job.description");

        log.trace("Match with description: {}", job.description);
        
        String response = resumeMatcher.call_openai( profile.Content, job.description, prompt) ;
        log.trace("AI Response: {}", response);

        return response;
    }

}
