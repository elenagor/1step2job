package com.ostj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.ostj.entities.PersonPositionEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ostj.dataaccess.MatchResultReceiver;
import com.ostj.dataaccess.MatchResultsNotifyBulder;
import com.ostj.dataproviders.PersonProvider;
import com.ostj.dataproviders.PositionProvider;
import com.ostj.entities.Job_title;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.utils.EmailSender;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {
    private static Logger log = LoggerFactory.getLogger(KafkaStreamConfig.class);
    private static Gson gson = new GsonBuilder().create();

    @Value(value = "${spring.application.name}")
    String appName;

    @Value(value = "${ostj.kstream.topic.input}")
    String input_topic_name;

    @Value(value = "${ostj.kstream.topic.output}")
    String outputTopic;

    @Value(value = "${ostj.email.sender}")
    String emailSenderAddress;

    @Autowired
	ConfigurationHelper configHelper;

    private String bootstrapAddress;
    private float embeding_match_treshhold;
    private int overall_score_treshhold;

    @Autowired
    Serde<PersonPositionEvent> personPositionEvent;

    @Autowired
	PersonProvider personProvider;

    @Autowired
	PositionProvider positionProvider;

    @Autowired
    MatchResultReceiver resultManager;

    @Autowired
    MatchResultsNotifyBulder resultNotifyBuilder;

    @Autowired
    EmailSender emailSender;

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
        embeding_match_treshhold = Float.parseFloat( configHelper.getProperty("EMBEDDING_TRESHHOLD", "0.1"));
        overall_score_treshhold = Integer.parseInt( configHelper.getProperty("MATCH_TRESHHOLD", "5"));
        log.debug("Start embeding_match_treshhold={}, overall_score_treshhold={}", embeding_match_treshhold, overall_score_treshhold);

        KStream<String, String> stream = kStreamBuilder.stream(input_topic_name);
        stream.peek((k, v) -> {log.debug("RECEIVE key={}, value={}", k, v);})
        .mapValues(v -> mapStringValueToEventRecord(v))
        .filter((key, value) -> value != null )
        .mapValues(this::processPersonJob)
        .filter((k,v) -> {log.debug("Records for match processing: {}", v); return v != null;})
        // Splits pasing result into separate messages preparing for sending to further
		.flatMapValues(v -> v)
        .mapValues(v -> (PersonPositionEvent)v)
        .peek((k, v) -> {log.debug("SEND key={}, value={}", k, v);})
        .to(outputTopic, Produced.with(Serdes.String(), personPositionEvent ))
        ;
        return stream;
    }

    private PersonPositionEvent mapStringValueToEventRecord(String value) {
		log.trace("Message String Value {}", value);
		try {
            JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
			return gson.fromJson(jsonValue, PersonPositionEvent.class);
		} catch (Throwable e) {
			log.error("Error parsing json message {}", e);
		}
		return null;
	}

    private List<PersonPositionEvent> processPersonJob(String key, PersonPositionEvent value) {
        embeding_match_treshhold = Float.parseFloat( configHelper.getProperty("EMBEDDING_TRESHHOLD", "0.1"));
        overall_score_treshhold = Integer.parseInt( configHelper.getProperty("MATCH_TRESHHOLD", "5"));
        log.debug("Current embeding_match_treshhold={}, overall_score_treshhold={}", embeding_match_treshhold, overall_score_treshhold);
        
        log.debug("Processed key={}, value={}", key, value);
        List<PersonPositionEvent> list = new ArrayList<PersonPositionEvent>();
        Person person = new Person();
        try {
            if(value.isFinished){
                log.debug("Processed Finished person={}", value.PersonId);
                personProvider.getPersonData(value.PersonId, person);
                String notification = resultNotifyBuilder.createEmailBody(person, overall_score_treshhold);
                if(StringUtils.isNotBlank(notification)){
                    emailSender.withTO(person.email)
                                .withBody(notification)
                                .withSubject("1Step2Job found a job for you")
                                .send(emailSenderAddress);
                    log.debug("Sent email notification for person={}", person);
                }
                else{
                    log.info("There is not match result to notify person={}", value);
                }
            }
            else {
                if(value.PersonId > 0){
                    personProvider.getPersonData(value.PersonId, person);
                }
                if(value.ProfileId > 0){
                    personProvider.getPersonByProfileId(value.ProfileId, person);
                }
                if(person.profiles != null && person.profiles.size() > 0 && person.id > 0){
                    for(Profile profile : person.profiles){
                        for(Job_title title : profile.job_titles){
                            log.debug("Processed title: {}", title.title);
                            for( Position position : positionProvider.getPositionsByTitleComaring(person.id, profile.id, title.id, embeding_match_treshhold)){
                                log.debug("Found position: {}", position);
                                processEvent(profile, position, list);
                            }
                        }
                        list.add(new PersonPositionEvent(person.id, profile.id, -1, -1 , true));
                    }
                }
                else if(value.PositionId > 0 ){
                    Position position = new Position();
                    positionProvider.getPositionFromDB( value.PositionId, position);
                    log.debug("Processing posintion={}", position);
                    for(Person prsn : personProvider.getPersonByTitle(position.id, embeding_match_treshhold)){
                        log.debug("Found person: {}", prsn);
                        for(Profile profile : prsn.profiles){
                            processEvent(profile, position, list);
                            list.add(new PersonPositionEvent(prsn.id, profile.id, -1, -1 , true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error process key={} and value={}, {}", key, value, e);
        }
        return list.size() > 0 ? list : null;
    }

    private void processEvent(Profile profile, Position position, List<PersonPositionEvent> list){
        PersonPositionEvent event = new PersonPositionEvent(profile.person_id, profile.id, position.id, -1, false);
        if(isEventNotExistInList(list, event)){
            list.add(event);
            resultManager.saveMatchResult(event);
        }
    }

    private boolean isEventNotExistInList(List<PersonPositionEvent> list, PersonPositionEvent newEvent) {
        for(PersonPositionEvent event: list){
            if(event.equals(newEvent)){
                return false;
            }
        }
        return true;
    }
}
