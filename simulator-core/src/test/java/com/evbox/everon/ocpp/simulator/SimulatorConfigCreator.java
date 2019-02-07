package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;

import static java.util.Arrays.asList;

public class SimulatorConfigCreator {

    public static SimulatorConfiguration createSimulatorConfiguration(SimulatorConfiguration.Station... stationConfigurations) {
        SimulatorConfiguration configuration = new SimulatorConfiguration();
        configuration.setStations(asList(stationConfigurations));
        return configuration;
    }

    public static SimulatorConfiguration.Station createStationConfiguration(String stationId, int evseCount, int connectorsPerEvse) {
        SimulatorConfiguration.Station station = new SimulatorConfiguration.Station();

        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(evseCount);
        evse.setConnectors(connectorsPerEvse);

        station.setId(stationId);
        station.setEvse(evse);
        return station;
    }
}
