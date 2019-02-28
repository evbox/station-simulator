package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.evbox.everon.ocpp.simulator.support.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.simulator.support.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;

public class StationSimulatorSetUp {

    protected OcppServerClient ocppServerClient = new OcppServerClient();

    protected OcppMockServer ocppMockServer = OcppMockServer.builder()
            .hostname(HOST)
            .port(PORT)
            .path(PATH)
            .ocppServerClient(ocppServerClient)
            .build();

    protected StationSimulatorRunner stationSimulatorRunner;

    @BeforeEach
    void setUp() {
        ocppMockServer.start();

        SimulatorConfiguration.StationConfiguration stationConfiguration = createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS);
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);
    }

    @AfterEach
    void tearDown() {
        ocppMockServer.stop();
        ocppMockServer.reset();
    }
}
