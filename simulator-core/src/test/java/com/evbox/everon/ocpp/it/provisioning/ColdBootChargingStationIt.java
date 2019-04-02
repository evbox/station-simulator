package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.ocpp.exchange.BootNotification;
import com.evbox.everon.ocpp.mock.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.ocpp.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.OCPP_SERVER_URL;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.awaitility.Awaitility.await;

public class ColdBootChargingStationIt extends StationSimulatorSetUp {

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

        ocppMockServer
                .when(StatusNotification.request(AVAILABLE), times(4))
                .thenReturn(TransactionEvent.response());

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
