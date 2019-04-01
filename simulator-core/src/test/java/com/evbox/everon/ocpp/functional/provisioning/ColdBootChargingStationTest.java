package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.BootNotification;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.testutils.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutils.constants.StationConstants.OCPP_SERVER_URL;
import static com.evbox.everon.ocpp.testutils.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutils.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.testutils.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.testutils.factory.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.awaitility.Awaitility.await;

public class ColdBootChargingStationTest extends StationSimulatorSetUp {

    private static String ADDITIONAL_STATION_ID = "EVB-P18090564";

    @Test
    void shouldStartStation() {

        ocppMockServer
                .when(BootNotification.request())
                .thenReturn(BootNotification.response());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldStartMultipleStations() {

        ocppMockServer
                .when(BootNotification.request(), times(2))
                .thenReturn(BootNotification.response());

        ocppMockServer.expectRequestFromStation(StatusNotification.request(AVAILABLE), times(4));

        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID, 1, 1),
                createStationConfiguration(ADDITIONAL_STATION_ID, 1, 3));

        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}