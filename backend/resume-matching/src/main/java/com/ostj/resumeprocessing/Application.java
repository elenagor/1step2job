package com.ostj.resumeprocessing;

import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.ostj.OpenAIProvider;
import com.ostj.dataaccess.JobsReceiver;
import com.ostj.dataaccess.PersonReceiver;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.utils.ConfigurationHelper;

@SpringBootApplication
public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);
    private Properties props;
    private ConfigProvider configProvider;

    @Value("${config}")
	String config;

    public static void main(String[] args) {
        log.debug("Start Resume-matching application with args:", Arrays.toString(args));
        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean 
    public ConfigProvider getConfigProvider(){
        if (props == null) {
			props = getPropertiesConfiguration();
		}
		log.trace("Start ConfigProvider Bean");
        configProvider = new ConfigProvider(props);
        return configProvider;
    }

    @Bean
    public SQLAccess getSQLAccess() {
        log.debug("SQLAccess: jdbcUrl=" + configProvider.getJdbcUrl() + ",username=" + configProvider.getUsername() + ",password=" + configProvider.getPassword());
    	try {
            return new SQLAccess(configProvider.getJdbcUrl(), configProvider.getUsername(), configProvider.getPassword());
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }
    
    @Bean
    public OpenAIProvider getOpenAIProvider() {
        log.debug("AI Matcher: apiKey=" + configProvider.getApiKey() + ",endpoint=" + configProvider.getEndpoint() + ",model=" + configProvider.getModel());
    	return new OpenAIProvider(configProvider.getApiKey(), configProvider.getEndpoint(), configProvider.getModel());
    }

    @Bean
    public PromptManager getPromptManager() {
    	return new PromptManager();
    }

    @Bean
    public PersonReceiver getPersonReceiver() {
        log.debug("PersonReceiver: jdbcUrl=" + configProvider.getJdbcUrl() + ",username=" + configProvider.getUsername() + ",password=" + configProvider.getPassword());
    	try {
            return new PersonReceiver(configProvider.getJdbcUrl(), configProvider.getUsername(), configProvider.getPassword());
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public JobsReceiver getJobsReceiver()  {
        log.debug("JobsReceiver: jdbcUrl=" + configProvider.getJdbcUrl() + ",username=" + configProvider.getUsername() + ",password=" + configProvider.getPassword());
    	try {
            return new JobsReceiver(configProvider.getJdbcUrl(), configProvider.getUsername(), configProvider.getPassword());
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public ResultManager getResultManager()  {
    	return new ResultManager();
    }

    private Properties getPropertiesConfiguration() {
		if (StringUtils.isEmpty(config)) {
			log.error("configuratin file is not specified, re-invoke with --config=<file> parameter");
			throw new RuntimeException("Congifuration file paramenter is not specified");
		}
		log.trace("Start Configuration Properties");
		props = ConfigurationHelper.loadPropertiesFromConfiguration(config);
		return props;
	}
}
