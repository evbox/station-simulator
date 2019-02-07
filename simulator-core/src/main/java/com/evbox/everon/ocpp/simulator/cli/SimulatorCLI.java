package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.RunConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

public class SimulatorCLI implements Callable<RunConfiguration> {

    @CommandLine.Parameters(index = "0", paramLabel = "URL", description = "URL (e.g. 'ws://ocpp.local.everon.io:8083/ocpp')")
    private String url;

    @CommandLine.Option(names = {"-f", "--configurationFile"})
    private File configurationFile;

    @CommandLine.Option(names = {"-c", "--configuration"}, converter = SimulatorConfigurationConverter.class)
    private SimulatorConfiguration simulatorConfiguration;

    @CommandLine.Option(names = {"-p", "--printConfiguration"}, description = "Print effective simulator configuration on start", defaultValue = "false")
    private boolean printConfiguration;

    @Override
    public RunConfiguration call() {
        return new RunConfiguration(url, configurationFile, simulatorConfiguration, printConfiguration);
    }

}
