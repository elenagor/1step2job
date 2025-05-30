package com.ostj.resumeprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.ostj.dataaccess.JobsReceiver;
import com.ostj.dataaccess.PersonReceiver;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.ResultManager;
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
    public SQLAccess getSQLAccess() {
        log.debug("SQLAccess: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new SQLAccess(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }
    
    @Bean
    public AIMatcher getMatcher() {
        log.debug("AI Matcher: apiKey=" + apiKey + ",endpoint=" + endpoint + ",model=" + model);
    	return new AIMatcher(apiKey, endpoint, model);
    }

    @Bean
    public PromptManager getPromptManager() {
    	return new PromptManager();
    }

    @Bean
    public PersonReceiver getPersonReceiver() {
        log.debug("PersonReceiver: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new PersonReceiver(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public JobsReceiver getJobsReceiver()  {
        log.debug("JobsReceiver: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new JobsReceiver(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public ResultManager getResultManager()  {
    	return new ResultManager();
    }
}
