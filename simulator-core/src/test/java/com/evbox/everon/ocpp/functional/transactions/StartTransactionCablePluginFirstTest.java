package com.evbox.everon.ocpp.functional.transactions;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.TransactionEvent;
import com.evbox.everon.ocpp.testutils.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutils.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutils.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static org.awaitility.Awaitility.await;

public class StartTransactionCablePluginFirstTest extends StationSimulatorSetUp {

    @Test
    void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {

        ocppMockServer
                .expectRequestFromStation(StatusNotification.request(OCCUPIED), atLeastOnce())
                .expectRequestFromStation(TransactionEvent.request(STARTED, DEFAULT_SEQ_NUMBER, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID));

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
