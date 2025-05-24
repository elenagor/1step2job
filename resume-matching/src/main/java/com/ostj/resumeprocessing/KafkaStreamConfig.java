package com.ostj.resumeprocessing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ostj.entity.Job;
import com.ostj.entity.Person;
import com.ostj.entity.Resume;
import com.ostj.entity.ResumeProcessEvent;

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

    @Autowired
	Matcher resumeMatcher;

    @Autowired
	SQLAccess dbConnector;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        log.trace("kStreamsConfig: appName="+appName+",bootstrapAddress="+bootstrapAddress+",topic_name="+topic_name);
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
        System.out.println("\nvalue="+value);
		ResumeProcessEvent record = null;
		try {
            JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
			record = gson.fromJson(jsonValue, ResumeProcessEvent.class);
            log.trace("Message mapped Value: " + record.toString());
		} catch (Throwable e) {
			log.error("Error parsing json message " + e);
		}
		return record;
	}

    private void processMessage(String key, ResumeProcessEvent record) {
        System.out.println("key="+key+",value="+record);
        try{
            String response = "";
            if(record.PersonId != -1){
                List<Person> persons = dbConnector.getPersonData(record.PersonId );
                if(persons != null){
                    for(Person person : persons){
                        log.debug("Found person: {}", person.toString());
                        for(Resume resume : person.resumes){
                            log.trace("Process resume: {}", resume.Content);
                            if(record.JobId.length() > 0 ){
                                Job job = dbConnector.getJob(record.JobId);
                                response = call_openai(record, resume, job ) ;
                            }
                            else{
                                List<Job> jobs = dbConnector.getJobs(person);
                                for(Job job : jobs){
                                    response = call_openai(record, resume, job ) ;
                                }
                            }
                        }
                    }
                }
            }
            else{
                response = resumeMatcher.run( record.resumeFilePath,  record.jdFilePath,  getPromt(record.promptFilePath));
                log.debug("Response: " + response);
            }
        }
        catch(Throwable e){
            log.error("Error: " + e.toString());
        }
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
        log.trace("Response: " + response);
        
        return response;
    }
    
    private String getPromt(String promptResourceName) throws IOException{
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(promptResourceName),  "UTF-8");
    }
}
