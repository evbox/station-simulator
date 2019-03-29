package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.testutil.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.OCPP_SERVER_URL;
import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.*;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.bootNotificationResponseMock;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.emptyResponse;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.awaitility.Awaitility.await;

public class ColdBootChargingStationTest extends StationSimulatorSetUp {

    private static String ADDITIONA_STATION_ID = "EVB-P18090564";

    @Test
    void shouldStartStation() {

        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldStartMultipleStations() {

        ocppMockServer
                .when(bootNotificationRequest(), times(2))
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE), times(4))
                .thenReturn(emptyResponse());

        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID, 1, 1),
                createStationConfiguration(ADDITIONA_STATION_ID, 1, 3));

        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}