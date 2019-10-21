package com.evbox.everon.ocpp.simulator.configuration;

import com.evbox.everon.ocpp.simulator.configuration.exception.ConfigurationException;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;

import java.io.File;
import java.io.IOException;

public class ConfigurationFileReader {

    public static SimulatorConfiguration read(File configurationFile) {
        if (!configurationFile.exists()) {
            throw new ConfigurationException("File does not exist: " + configurationFile.getAbsolutePath());
        }

        try {
            return ObjectMapperHolder.YAML_OBJECT_MAPPER.readValue(configurationFile, SimulatorConfiguration.class);
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read configuration from file", e);
        }
    }
}
