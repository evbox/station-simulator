package com.evbox.everon.ocpp.it.availability;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.mock.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.ocpp.exchange.TransactionEvent;
import com.evbox.everon.ocpp.mock.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static org.awaitility.Awaitility.await;

public class StatusNotificationIt extends StationSimulatorSetUp {

    @Test
    void shouldSendStatusNotificationRequestWhenChangeOccurs() {

        ocppMockServer
                .when(StatusNotification.request(OCCUPIED))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
