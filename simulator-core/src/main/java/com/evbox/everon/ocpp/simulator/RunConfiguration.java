package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.File;

@Value
@AllArgsConstructor
public class RunConfiguration {

    private String url;
    private File configurationFile;
    private SimulatorConfiguration simulatorConfiguration;
    private boolean printConfiguration;
}
