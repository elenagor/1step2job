package com.ostj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.ostj.managers.PositionManager;
import com.ostj.managers.PersonManager;

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
        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean
    public PersonManager getPersonManager() {
        log.debug("PersonReceiver: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new PersonManager(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public PositionManager getJobManager()  {
        log.debug("JobsReceiver: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new PositionManager(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }
/* 
    @Bean
    public AIMatcher getMatcher() {
        log.debug("AI Matcher: apiKey=" + apiKey + ",endpoint=" + endpoint + ",model=" + model);
    	return new AIMatcher(apiKey, endpoint, model);
    }
*/
}
