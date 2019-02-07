package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.cli.SimulatorCLI;
import com.evbox.everon.ocpp.simulator.configuration.ConfigurationFileReader;
import com.evbox.everon.ocpp.simulator.configuration.ConfigurationPrinter;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;

public class SimulatorApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorApp.class);

    public static void main(String[] args) {
        RunConfiguration runConfiguration = CommandLine.call(new SimulatorCLI(), args);

        Optional<SimulatorConfiguration> consoleConfiguration = Optional.ofNullable(runConfiguration.getSimulatorConfiguration());
        Optional<SimulatorConfiguration> fileConfiguration = Optional.ofNullable(runConfiguration.getConfigurationFile()).map(ConfigurationFileReader::read);

        SimulatorConfiguration configuration = consoleConfiguration.orElseGet(() -> fileConfiguration.orElseThrow(() -> new ConfigurationException("No configuration provided")));

        LOGGER.info(consoleConfiguration.isPresent() ? "Using CONSOLE configuration" : "Using FILE configuration");

        if (runConfiguration.isPrintConfiguration()) {
            ConfigurationPrinter.printConfiguration(runConfiguration, configuration);
        }

        StationSimulator stationSimulator = new StationSimulator(runConfiguration.getUrl(), configuration);
        stationSimulator.start();
    }

}
