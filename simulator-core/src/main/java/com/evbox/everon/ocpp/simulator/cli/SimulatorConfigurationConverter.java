package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.exception.ConfigurationException;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;
import picocli.CommandLine;

import java.io.IOException;

public class SimulatorConfigurationConverter implements CommandLine.ITypeConverter<SimulatorConfiguration> {

    @Override
    public SimulatorConfiguration convert(String value) {
        // Since double quotes are trimmed by shell, configuration is passed with single quotes,
        // which requires this post-processing step
        String valueWithDoubleQuotes = value.replace("'", "\"");
        try {
            return ObjectMapperHolder.getJsonObjectMapper().readValue(valueWithDoubleQuotes, SimulatorConfiguration.class);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
}
