package com.ostj;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.ostj.dataaccess.MatchResultReceiver;
import com.ostj.dataaccess.MatchResultsNotifyBulder;
import com.ostj.dataproviders.PersonProvider;
import com.ostj.dataproviders.PositionProvider;
import com.ostj.utils.EmailSender;

@SpringBootApplication
public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);
    private ConfigurationHelper configHelper;

    @Value("${config}")
	String config;

    private String jdbcUrl;
    private String username;
    private String password;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean 
    public ConfigurationHelper getConfigurationHelper() throws Exception{
        if (StringUtils.isEmpty(config)) {
            log.error("configuratin file is not specified, re-invoke with --config=<file> parameter");
            throw new RuntimeException("Congifuration file paramenter is not specified");
        }
        try {
            configHelper = new ConfigurationHelper(config);
            jdbcUrl = configHelper.getProperty("DB_URL", "");
            username = configHelper.getProperty("DB_USER", "");
            password = configHelper.getProperty("DB_PASSWORD", "");
            return configHelper;

        } catch (Exception e) {
            log.error("Error load config file {}", e);
            throw e;
        }
    }

    @Bean
    public PersonProvider getPersonManager() throws Exception {
        log.debug("PersonManager: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new PersonProvider(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
            throw e;
        }
    }

    @Bean
    public PositionProvider getJobManager()  {
        log.debug("PositionManager: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new PositionProvider(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public MatchResultReceiver getMatchResultReceiver(){
        log.debug("MatchResultReceiver: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new MatchResultReceiver(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public MatchResultsNotifyBulder getMatchResultsNotifyBulder(){
        log.debug("MatchResultsNotifyBulder: jdbcUrl=" + jdbcUrl + ",username=" + username + ",password=" + password);
    	try {
            return new MatchResultsNotifyBulder(jdbcUrl, username, password);
        } catch (Exception e) {
            log.error("Error connect to DB {}", e);
        }
        return null;
    }

    @Bean
    public EmailSender getEmailSender(){
        EmailSender emailSender =  EmailSender.getEmailSender(  configHelper.getProperty("SMTP_ADDRESS", ""),  
                                            configHelper.getProperty("EMAIL_ACCOUNT_NAME", ""),  
                                            configHelper.getProperty("EMAIL_ACCOUNT_PASSWORD", ""));
        emailSender.setPort(configHelper.getProperty("SMTP_PORT", ""));
        emailSender.setUseSSL(configHelper.getProperty("SMTP_USESSL", ""));
        return emailSender;
    }
}
