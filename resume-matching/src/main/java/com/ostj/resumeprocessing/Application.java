package com.ostj.resumeprocessing;

import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.ostj.dataaccess.JobManager;
import com.ostj.dataaccess.PersonManager;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.openai.AIMatcher;

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
    public AIMatcher getMatcher() {
        log.debug("Matcher: apiKey=" + apiKey + ",endpoint=" + endpoint + ",model=" + model);
    	return new AIMatcher(apiKey, endpoint, model);
    }
        
    @Bean
    public SQLAccess getDBConnector() throws SQLException {
        log.debug("SQLAccess: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	return new SQLAccess(jdbcUrl, username, password);
    }

    @Bean
    public PromptManager getPromptManager() {
    	return new PromptManager();
    }

    @Bean
    public PersonManager getPersonManager() {
    	return new PersonManager();
    }

    @Bean
    public JobManager getJobManager()  {
    	return new JobManager();
    }
}
