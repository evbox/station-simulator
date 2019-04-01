package com.evbox.everon.ocpp.testutils.factory;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;

import static java.util.Arrays.asList;

public class SimulatorConfigCreator {

    public static SimulatorConfiguration createSimulatorConfiguration(SimulatorConfiguration.StationConfiguration... stationConfigurations) {
        SimulatorConfiguration configuration = new SimulatorConfiguration();
        configuration.setStations(asList(stationConfigurations));
        return configuration;
    }

    public static SimulatorConfiguration.StationConfiguration createStationConfiguration(String stationId, int evseCount, int connectorsPerEvse) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();

        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(evseCount);
        evse.setConnectors(connectorsPerEvse);

        stationConfiguration.setId(stationId);
        stationConfiguration.setEvse(evse);
        return stationConfiguration;
    }
}
