package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.BASIC_AUTH_PASSWORD;
import static java.util.Arrays.asList;

public class SimulatorConfigCreator {

    public static SimulatorConfiguration createSimulatorConfiguration(SimulatorConfiguration.StationConfiguration... stationConfigurations) {
        SimulatorConfiguration configuration = new SimulatorConfiguration();
        configuration.setStations(asList(stationConfigurations));
        return configuration;
    }

    public static SimulatorConfiguration.StationConfiguration createStationConfiguration(String stationId, int evseCount, int connectorsPerEvse) {
        return createStationConfiguration(stationId, evseCount, connectorsPerEvse, SimulatorConfiguration.MeterValuesConfiguration.builder().build());
    }

    public static SimulatorConfiguration.StationConfiguration createStationConfiguration(String stationId, int evseCount, int connectorsPerEvse, SimulatorConfiguration.MeterValuesConfiguration meterValuesConfiguration) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();

        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(evseCount);
        evse.setConnectors(connectorsPerEvse);

        stationConfiguration.setId(stationId);
        stationConfiguration.setEvse(evse);
        stationConfiguration.getComponentsConfiguration().getSecurityCtrlr().setBasicAuthPassword(BASIC_AUTH_PASSWORD);
        stationConfiguration.setMeterValuesConfiguration(meterValuesConfiguration);
        return stationConfiguration;
    }

    public static SimulatorConfiguration.StationConfiguration createStationConfiguration(String stationId,
                                                                                         int evseCount,
                                                                                         int connectorsPerEvse,
                                                                                         String password) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = new SimulatorConfiguration.StationConfiguration();

        SimulatorConfiguration.Evse evse = new SimulatorConfiguration.Evse();
        evse.setCount(evseCount);
        evse.setConnectors(connectorsPerEvse);

        stationConfiguration.setId(stationId);
        stationConfiguration.setEvse(evse);
        stationConfiguration.getComponentsConfiguration().getSecurityCtrlr().setBasicAuthPassword(password);
        return stationConfiguration;
    }
}
