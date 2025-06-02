package com.ostj.utils;

import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfigurationHelper {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHelper.class);

    public static Properties loadPropertiesFromConfiguration(String fileName) {
        Properties prop = new Properties();
		Parameters params = new Parameters();
		try {
			FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName(fileName));
			Configuration config = builder.getConfiguration();
			prop = ConfigurationConverter.getProperties(config);
		} catch (Throwable ex) {
			log.error("Error loading properties/configuration from resource: " + ex);
		}

        return prop;
    }

    
}
