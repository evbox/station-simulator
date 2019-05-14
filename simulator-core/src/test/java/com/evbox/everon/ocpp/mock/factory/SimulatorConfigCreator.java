package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.BASIC_AUTH_PASSWORD;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.BASIC_AUTH_USERNAME;
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
        stationConfiguration.setUsername(BASIC_AUTH_USERNAME);
        stationConfiguration.setPassword(BASIC_AUTH_PASSWORD);
        return stationConfiguration;
    }

    public static SimulatorConfiguration.StationConfiguration createStationConfiguration(String stationId,
                                                                                         int evseCount,
                                                                                         int connectorsPerEvse,
                                                                                         String username,
                                                                                         String password) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();

        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(evseCount);
        evse.setConnectors(connectorsPerEvse);

        stationConfiguration.setId(stationId);
        stationConfiguration.setEvse(evse);
        stationConfiguration.setUsername(username);
        stationConfiguration.setPassword(password);
        return stationConfiguration;
    }
}
