package com.ostj.resumeprocessing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.io.IOUtils;
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

import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Job;
import com.ostj.dataentity.MatchResult;
import com.ostj.dataentity.Person;
import com.ostj.dataentity.Resume;
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
        .peek((key, value) -> processMessage(key, value));
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
            if(record.PersonId != -1){
                 processPersonMessage(record);
            }
            else{
                // backdoor: work with files from folder, but not db
                String response = resumeMatcher.run( record.resumeFilePath,  record.jdFilePath,  getPromt(record.promptFilePath));
                log.debug("Response: " + response);
            }
            
        }
        catch(Throwable e){
            log.error("Error: " + e.toString());
        }
    }

    private void processPersonMessage(ResumeProcessEvent record) throws Exception{
        List<Person> persons = dbConnector.getPersonData(record.PersonId );
        if(persons == null){
            throw new Exception(  String.format("There is no person with id=%d", record.PersonId));
        }
        for(Person person : persons){
            log.debug("Processed person: {}", person.toString());
            for(Resume resume : person.resumes){
                log.trace("Processed resume: {}", resume.Content);
                if(record.JobId.length() > 0 ){
                    Job job = dbConnector.getJob(record.JobId);
                    log.debug("Found Job: {}", job);
                    processResume( record,  person,  resume,  job);
                }
                else{
                    List<Job> jobs = dbConnector.getJobs(person);
                    log.debug("Found {} Jobs", jobs.size());
                    int savedMatchResults = 0;
                    for(Job job : jobs){
                       savedMatchResults = savedMatchResults + processResume( record,  person,  resume,  job);
                    }
                    log.debug("Saved {} results", savedMatchResults);
                }
            }
        }
    }
    private int processResume(ResumeProcessEvent record, Person person, Resume resume, Job job){
        try{
            String response = call_openai(record, resume, job ) ;
            MatchResult result = convertResponce(person, resume, job, response);
            if(result.overall_score >= match_treshhold ){
                log.debug("Save result to DB {}", result);
                return dbConnector.saveMatchResult(result);
            }
        }
        catch(Exception e){
            log.error("Error on proces resumeId={} jobId={} {}", resume.Id, job.Id, e);
        }
        return 0;
    }

    private MatchResult  convertResponce(Person person, Resume resume, Job job, String response){
        log.trace("AI Responce: {}", response);
        JsonObject jsonValue = JsonParser.parseString(response).getAsJsonObject();
        log.trace("Json Responce: {}", jsonValue);
        MatchResult  result = gson.fromJson(jsonValue, MatchResult .class);
        result.PersonId = person.Id;
        result.ResumeId = resume.Id;
        result.JobId = job.Id;
        log.debug("MatchResult: {}", result);
        return result;
    }

    private String call_openai(ResumeProcessEvent record, Resume resume, Job job) throws Exception{
        if(record == null)
            throw new MissingArgumentException("record.prompt");
        if(resume == null)
            throw new MissingArgumentException("resume.content");
        if(job == null)
            throw new MissingArgumentException("job.description");

        log.trace("Match with description: {}", job.description);
        String response = resumeMatcher.call_openai( resume.Content, job.description, getPromt(record.promptFilePath)) ;
        log.trace("AI Response: {}", response);

        return response;
    }
    
    private String getPromt(String promptResourceName) throws IOException{
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(promptResourceName),  "UTF-8");
    }
}
