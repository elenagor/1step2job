package com.ostj.resumeprocessing;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);
	@Value(value = "${ostj.openai.apikey}")
	String apiKey;

	@Value(value = "${ostj.openai.endpoint}")
	String endpoint;

	@Value(value = "${ostj.openai.model}")
	String model;

    public static void main(String[] args) {
        log.trace("Trace log message");
        log.debug("Debug log message");
        log.info("Info log message");
        log.error("Error log message");

        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            //log.info(beanName);
        }
        };
    }
    
    @Bean
    public Matcher getMatcher() {
    	return new Matcher(apiKey, endpoint, model);
    }
}
