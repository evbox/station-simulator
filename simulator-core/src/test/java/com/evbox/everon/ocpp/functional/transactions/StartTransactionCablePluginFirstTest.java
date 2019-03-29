package com.evbox.everon.ocpp.functional.transactions;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.testutil.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.statusNotificationRequest;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.transactionEventRequest;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static org.awaitility.Awaitility.await;

public class StartTransactionCablePluginFirstTest extends StationSimulatorSetUp {

    @Test
    void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {

        ocppMockServer
                .expectRequestFromStation(statusNotificationRequest(OCCUPIED), atLeastOnce())
                .expectRequestFromStation(transactionEventRequest(STARTED, 0, "1", DEFAULT_EVSE_ID));

        ocppMockServer.expectRequestFromStation(transactionEventRequest(STARTED, 0, "1", DEFAULT_EVSE_ID));

        stationSimulatorRunner.run();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
