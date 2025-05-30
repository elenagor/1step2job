package com.ostj;

import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.ostj.events.ProcessEvent;

@Configuration
public class SerdeConfiguration {
    
    @Bean
	public Serde<ProcessEvent> getProcessEventSerde() {
		return new JsonSerde<>(ProcessEvent.class);
	}
}
