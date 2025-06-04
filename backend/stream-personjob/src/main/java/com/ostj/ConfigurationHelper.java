package com.ostj;

import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHelper {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHelper.class);
    private Properties props;

    public ConfigurationHelper(String fileName) throws ConfigurationException {
		Parameters params = new Parameters();
        log.debug("Start ConfigurationHelper");
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class).configure(params.properties().setFileName(fileName));
        Configuration config = builder.getConfiguration();
        props = ConfigurationConverter.getProperties(config);

    }

    public String getProperty(String key, String defValue) {
        return props.getProperty(key, defValue);
    }
}
