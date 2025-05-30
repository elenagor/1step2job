package com.ostj;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.ostj.events.ProcessEvent;

@SpringBootApplication
public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);
    @Value(value = "${ostj.openai.apikey}")
	String apiKey;

	@Value(value = "${ostj.openai.endpoint}")
	String endpoint;

	@Value(value = "${ostj.openai.model}")
	String model;

    @Value(value = "${ostj.db.url}")
    String jdbcUrl;

    @Value(value = "${ostj.db.username}")
    String username;

    @Value(value = "${ostj.db.password}")
    String password;

    @Value(value = "${ostj.kstream.topic.output}")
    String outputTopic;

    @Autowired
    Serde<ProcessEvent> messageSerdersEvent;

    public static void main(String[] args) {
        log.trace("Trace log message");
        log.debug("Debug log message");
        log.info("Info log message");
        log.error("Error log message");

        new SpringApplicationBuilder(Application.class).run(args);
    }
    @SuppressWarnings("unchecked")
	@Bean
	public Consumer<KStream<String, String>> process() {
		log.trace("Initializing Kafka Streams topology");
		return input -> input
        .mapValues(this::processPersonJob)
        .filter((k,v) -> {return v != null;})
        .split()
        .branch((k, v) -> (v instanceof ProcessEvent), Branched.withConsumer(stream -> stream
						// Convert basic Serializable to relevant type
						.mapValues(v -> (ProcessEvent) v)
                        .peek((k, v) -> {log.debug("key={}, value={}", k, v);})
                        .to(outputTopic, Produced.with(Serdes.String(), messageSerdersEvent )))
        );
    }

    private List<ProcessEvent> processPersonJob(String key, String value) {
        log.debug("key={}, value={}", key, value);
        List<ProcessEvent> list = new ArrayList<ProcessEvent>();
        ProcessEvent event = new ProcessEvent();
        event.PersonId = 1;
        event.ProfileId = 1;
        event.JobId = 1;
        list.add(event);
        return list.size() > 0 ? list : null;
    }
}
