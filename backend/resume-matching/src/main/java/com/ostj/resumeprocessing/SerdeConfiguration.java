package com.ostj.resumeprocessing;

import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.ostj.resumeprocessing.events.ResumeProcessEvent;



@Configuration
public class SerdeConfiguration {
    
    @Bean
	public Serde<ResumeProcessEvent> getResumeProcessEvent() {
		return new JsonSerde<>(ResumeProcessEvent.class);
	}
}
