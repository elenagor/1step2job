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

import com.ostj.entities.Position;
import com.ostj.dataproviders.PersonProvider;
import com.ostj.dataproviders.PositionProvider;
import com.ostj.entities.Job_title;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.events.ProcessEvent;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);

    @Value(value = "${spring.application.name}")
    String appName;

    @Value(value = "${ostj.kstream.topic.input}")
    String input_topic_name;

    @Value(value = "${ostj.kstream.topic.output}")
    String outputTopic;

    @Autowired
	ConfigurationHelper configHelper;

    private String bootstrapAddress;
    private float embeding_match_treshhold;

    @Autowired
    Serde<ProcessEvent> messageSerdersEvent;

    @Autowired
	PersonProvider personProvider;

    @Autowired
	PositionProvider positionProvider;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        bootstrapAddress = configHelper.getProperty("KAFKA_SERVER", "");
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        log.debug("kStreamsConfig: appName={}, bootstrapAddress={}, topic_name={}", appName, bootstrapAddress, input_topic_name);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String,String> kStream(StreamsBuilder kStreamBuilder){
        embeding_match_treshhold = Float.parseFloat( configHelper.getProperty("MATCH_TRESHHOLD", "0.1"));
        log.debug("Currently embeding_match_treshhold={}", embeding_match_treshhold);

        KStream<String, String> stream = kStreamBuilder.stream(input_topic_name);
        stream.peek((k, v) -> {log.debug("Recieved key={}, value={}", k, v);})
        .mapValues(this::processPersonJob)
        .filter((k,v) -> {log.debug("Records for match processing: {}", v); return v != null;})
        // Splits pasing result into separate messages preparing for sending to further
		.flatMapValues(v -> v)
        .mapValues(v -> (ProcessEvent)v)
        .peek((k, v) -> {log.debug("Sent key={}, value={}", k, v);})
        .to(outputTopic, Produced.with(Serdes.String(), messageSerdersEvent ))
        ;
        return stream;
    }

    private List<ProcessEvent> processPersonJob(String key, String value) {
        embeding_match_treshhold = Float.parseFloat( configHelper.getProperty("MATCH_TRESHHOLD", "0.1"));
        log.debug("Currently embeding_match_treshhold={}", embeding_match_treshhold);
        
        log.debug("Processed key={}, value={}", key, value);
        List<ProcessEvent> list = new ArrayList<ProcessEvent>();
        Person person = new Person();
        try {
            if(key.equalsIgnoreCase("PersonId")){
                personProvider.getPersonData(Integer.parseInt(value), person);
            }
            if(key.equalsIgnoreCase("ProfileId")){
                personProvider.getPersonByProfileId(Integer.parseInt(value), person);
            }
            if(person.profiles != null){
                for(Profile profile : person.profiles){
                    for(Job_title title : profile.job_titles){
                        log.debug("Processed title: {}", title.title);
                        for( Position position : positionProvider.getPositionsByTitleComaring(person.id, profile.id, title.id, embeding_match_treshhold)){
                            log.debug("Found position: {}", position);
                            ProcessEvent event = new ProcessEvent(profile.person_id, profile.id, position.id, -1);
                            if(isEventNotExist(list, event)){
                                list.add(event);
                            }
                        }
                    }
                }
            }
            if(key.equalsIgnoreCase("PositionId")){
                Position position = new Position();
                positionProvider.getPositionFromDB( Integer.parseInt(value), position);
                for(Person prsn : personProvider.getPersonByTitle(position.id, embeding_match_treshhold)){
                    log.debug("Found person: {}", prsn);
                    for(Profile profile : prsn.profiles){
                        ProcessEvent event = new ProcessEvent(profile.person_id, profile.id, position.id, -1);
                        if(isEventNotExist(list, event)){
                            list.add(event);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error process key={} and value={}, {}", key, value, e);
        }
        return list.size() > 0 ? list : null;
    }

    private boolean isEventNotExist(List<ProcessEvent> list, ProcessEvent newEvent) {
        for(ProcessEvent event: list){
            if(event.equals(newEvent)){
                return false;
            }
        }
        return true;
    }
}
