package com.evbox.everon.ocpp.testutils.station;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.evbox.everon.ocpp.testutils.constants.StationConstants;
import com.evbox.everon.ocpp.testutils.factory.SimulatorConfigCreator;
import com.evbox.everon.ocpp.testutils.ocpp.OcppMockServer;
import com.evbox.everon.ocpp.testutils.ocpp.OcppServerClient;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.BootNotification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.testutils.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutils.expect.ExpectedCount.any;

public class StationSimulatorSetUp  {

    private static final int ASSERT_TIMEOUT_IN_SECONDS = 10;

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
        Awaitility.setDefaultTimeout(ASSERT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        ocppMockServer.start();

        SimulatorConfiguration.StationConfiguration stationConfiguration = SimulatorConfigCreator.createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS);
        SimulatorConfiguration simulatorConfiguration = SimulatorConfigCreator.createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(StationConstants.OCPP_SERVER_URL, simulatorConfiguration);

        // Station always needs boot notification message on startup
        ocppMockServer
                .when(BootNotification.request(), any())
                .thenReturn(BootNotification.response());
    }

    @AfterEach
    void tearDown() {
        ocppMockServer.stop();
        ocppMockServer.reset();
    }

    protected void triggerUserAction(String stationId, UserMessage action) {
        stationSimulatorRunner.getStation(stationId).sendMessage(new StationMessage(stationId, StationMessage.Type.USER_ACTION, action));
    }
}
