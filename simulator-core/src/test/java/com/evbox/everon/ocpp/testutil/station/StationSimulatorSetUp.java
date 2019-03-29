package com.evbox.everon.ocpp.testutil.station;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.evbox.everon.ocpp.testutil.constants.StationConstants;
import com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator;
import com.evbox.everon.ocpp.testutil.ocpp.OcppMockServer;
import com.evbox.everon.ocpp.testutil.ocpp.OcppServerClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.bootNotificationRequest;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.bootNotificationResponseMock;

public class StationSimulatorSetUp  {

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

        SimulatorConfiguration.StationConfiguration stationConfiguration = SimulatorConfigCreator.createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS);
        SimulatorConfiguration simulatorConfiguration = SimulatorConfigCreator.createSimulatorConfiguration(stationConfiguration);

        ocppMockServer
                .when(bootNotificationRequest(), atLeastOnce())
                .thenReturn(bootNotificationResponseMock());

        stationSimulatorRunner = new StationSimulatorRunner(StationConstants.OCPP_SERVER_URL, simulatorConfiguration);
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
