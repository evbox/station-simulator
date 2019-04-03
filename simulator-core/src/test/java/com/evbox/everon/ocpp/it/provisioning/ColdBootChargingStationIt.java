package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.once;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.awaitility.Awaitility.await;

public class ColdBootChargingStationIt extends StationSimulatorSetUp {

    @Test
    void shouldStartStation() {

        stationBootCount = once();

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldStartMultipleStations() {

        stationBootCount = times(2);

        ocppMockServer
                .when(StatusNotification.request(AVAILABLE), times(2))
                .thenReturn(TransactionEvent.response());

        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(
                createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS),
                createStationConfiguration(ADDITIONAL_STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS));

        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
