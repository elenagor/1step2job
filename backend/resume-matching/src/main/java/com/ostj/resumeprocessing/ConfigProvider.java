package com.ostj.resumeprocessing;

import java.util.Properties;

import org.springframework.context.annotation.Bean;

public class ConfigProvider {
    private Properties _props;

	public ConfigProvider(Properties props) {
		_props = props;
    }

    @Bean
    public String getProperty(String key, String defaultValue){
        return _props.getProperty(key, defaultValue);
    }

    @Bean
    public String getApiKey(){
        return _props.getProperty("OPENAI_APIKEY", "");
    }

    @Bean
    public String getEndpoint(){
        return _props.getProperty("OPENAI_ENDPOINT", "");
    }

    @Bean
    public String getModel(){
        return _props.getProperty("OPENAI_MODEL", "");
    }

    @Bean
    public String getJdbcUrl(){
        return _props.getProperty("DB_URL", "");
    }

    @Bean
    public String getUsername(){
        return _props.getProperty("DB_USER", "");
    }

    @Bean
    public String getPassword(){
        return _props.getProperty("DB_PASSWORD", "");
    }

    @Bean
    public String getBootstrapAddress(){
        return _props.getProperty("KAFKA_SERVER", "");
    }
}
