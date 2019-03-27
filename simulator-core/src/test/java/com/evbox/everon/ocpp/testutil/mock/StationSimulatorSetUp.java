package com.evbox.everon.ocpp.testutil.mock;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.testutil.constants.StationConstants;
import com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class StationSimulatorSetUp {

    protected OcppServerClient ocppServerClient = new OcppServerClient();

    protected OcppMockServer ocppMockServer = OcppMockServer.builder()
            .hostname(StationConstants.HOST)
            .port(StationConstants.PORT)
            .path(StationConstants.PATH)
            .ocppServerClient(ocppServerClient)
            .build();

    protected StationSimulatorRunner stationSimulatorRunner;

    @BeforeEach
    void setUp() {
        ocppMockServer.start();

        SimulatorConfiguration.StationConfiguration stationConfiguration = SimulatorConfigCreator.createStationConfiguration(StationConstants.STATION_ID, StationConstants.DEFAULT_EVSE_COUNT, StationConstants.DEFAULT_EVSE_CONNECTORS);
        SimulatorConfiguration simulatorConfiguration = SimulatorConfigCreator.createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(StationConstants.OCPP_SERVER_URL, simulatorConfiguration);
    }

    @AfterEach
    void tearDown() {
        ocppMockServer.stop();
        ocppMockServer.reset();
    }
}
