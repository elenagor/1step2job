package com.ostj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.ostj.entities.Job;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.events.ProcessEvent;
import com.ostj.managers.JobManager;
import com.ostj.managers.PersonManager;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);

    @Value(value = "${ostj.kstream.topic.input}")
    String input_topic_name;

    @Value(value = "${spring.kafka.bootstrap-servers}")
    String bootstrapAddress;

    @Value(value = "${spring.application.name}")
    String appName;

    @Value(value = "${ostj.kstream.topic.output}")
    String outputTopic;

    @Autowired
    Serde<ProcessEvent> messageSerdersEvent;

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
        log.trace("kStreamsConfig: appName={}, bootstrapAddress={}, topic_name={}", appName, bootstrapAddress, input_topic_name);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String,String> kStream(StreamsBuilder kStreamBuilder){
        KStream<String, String> stream = kStreamBuilder.stream(input_topic_name);
        stream.peek((k, v) -> {log.debug("Recieved key={}, value={}", k, v);})
        .mapValues(this::processPersonJob)
        .filter((k,v) -> {log.debug("Will send: {}", v); return v != null;})
        // Splits pasing result into separate messages preparing for sending to further
		.flatMapValues(v -> v)
        .mapValues(v -> (ProcessEvent)v)
        .to(outputTopic, Produced.with(Serdes.String(), messageSerdersEvent ))
        ;
        return stream;
    }

    private List<ProcessEvent> processPersonJob(String key, String value) {
        log.debug("Processed key={}, value={}", key, value);
        List<ProcessEvent> list = new ArrayList<ProcessEvent>();
        Person person = new Person();
        try {
            if(key.equalsIgnoreCase("PersonId")){
                personManager.getPersonData(Integer.parseInt(value), person);
            }
            if(key.equalsIgnoreCase("ProfileId")){
                personManager.getPersonByProfileId(Integer.parseInt(value), person);
            }
            if(person.profiles != null){
                for(Profile profile : person.profiles){
                    log.debug("Processed title: {}", profile.title);
                    for( Job job : jobManager.getJobsWithTitle(profile.title)){
                        log.debug("Found Job: {}", job);
                        ProcessEvent event = new ProcessEvent(profile.person_id, profile.id, job.id, 1);
                        list.add(event);
                    }
                }
            }
            if(key.equalsIgnoreCase("JobId")){
                Job job = new Job();
                jobManager.getJobFromDB( Integer.parseInt(value), job);
                for(Person prsn : personManager.getPersonByTitle(job.title)){
                    log.debug("Found person: {}", prsn);
                    for(Profile profile : prsn.profiles){
                        ProcessEvent event = new ProcessEvent(profile.person_id, profile.id, job.id, 1);
                        list.add(event);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error process key={} and value={}, {}", key, value, e);
        }
        return list.size() > 0 ? list : null;
    }
}
