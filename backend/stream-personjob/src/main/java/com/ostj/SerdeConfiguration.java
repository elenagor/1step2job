package com.ostj;

import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.ostj.entities.PersonPositionEvent;

@Configuration
public class SerdeConfiguration {

    @Bean
	public Serde<PersonPositionEvent> getPersonPositionEvent() {
		return new JsonSerde<>(PersonPositionEvent.class);
	}

}
