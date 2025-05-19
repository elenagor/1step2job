package com.ostj.resumeprocessing;

import org.apache.kafka.streams.kstream.KStream;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ostj.entity.ResumeProcessEvent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig  {
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();

    @Value(value = "${ostj.kstream.topic}")
    String topic_name;

    @Value(value = "${spring.kafka.bootstrap-servers}")
    String bootstrapAddress;

    @Value(value = "${spring.application.name}")
    String appName;

    @Autowired
	Matcher resumeMatcher;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        System.out.println("kStreamsConfig: appName="+appName+",bootstrapAddress="+bootstrapAddress+",topic_name="+topic_name);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String,String> kStream(StreamsBuilder kStreamBuilder){
        KStream<String, String> stream = kStreamBuilder.stream(topic_name);
        stream.mapValues(value -> mapStringValueToEventRecord(value))
        .peek((key, value) -> processMessage(key, value));
        return stream;
    }

    private ResumeProcessEvent mapStringValueToEventRecord(String value) {
        System.out.println("value="+value);
		ResumeProcessEvent record = new ResumeProcessEvent();

		JsonObject jsonValue = JsonParser.parseString(value).getAsJsonObject();
		try {
			record = gson.fromJson(jsonValue, ResumeProcessEvent.class);
		} catch (Throwable e) {
			System.out.println("Error parsing json message " + e);
		}

		System.out.println("Message mapped Value: " + record.toString());
		return record;
	}

    private void processMessage(String key, ResumeProcessEvent record) {
        System.out.println("key="+key+",value="+record);
        try{
            String response = resumeMatcher.run_resume_matching( record.resumeFilePath,  record.jdFilePath,  record.promptFilePath);
            System.out.println("Response: " + response);
        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
    
}
