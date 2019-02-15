package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.cli.ConsoleReader;
import com.evbox.everon.ocpp.simulator.cli.SimulatorCLI;
import com.evbox.everon.ocpp.simulator.configuration.ConfigurationFileReader;
import com.evbox.everon.ocpp.simulator.configuration.ConfigurationPrinter;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;

public class SimulatorLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorLauncher.class);

    public static void main(String[] args) {
        RunConfiguration runConfiguration = CommandLine.call(new SimulatorCLI(), args);

        Optional<SimulatorConfiguration> consoleConfiguration = Optional.ofNullable(runConfiguration.getSimulatorConfiguration());
        Optional<SimulatorConfiguration> fileConfiguration = Optional.ofNullable(runConfiguration.getConfigurationFile()).map(ConfigurationFileReader::read);

        SimulatorConfiguration configuration = consoleConfiguration.orElseGet(() -> fileConfiguration.orElseThrow(() -> new ConfigurationException("No configuration provided")));

        LOGGER.info(consoleConfiguration.isPresent() ? "Using CONSOLE configuration" : "Using FILE configuration");

        if (runConfiguration.isPrintConfiguration()) {
            ConfigurationPrinter.printConfiguration(runConfiguration, configuration);
        }

        StationSimulatorRunner stationSimulatorRunner = new StationSimulatorRunner(runConfiguration.getUrl(), configuration);
        stationSimulatorRunner.run();

        // read data from StdInput
        ConsoleReader consoleReader = new ConsoleReader(stationSimulatorRunner.getStations());
        consoleReader.startReading();

    }

}
