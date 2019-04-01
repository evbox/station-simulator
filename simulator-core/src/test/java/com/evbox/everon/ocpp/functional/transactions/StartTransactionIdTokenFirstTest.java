package com.evbox.everon.ocpp.functional.transactions;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.Authorize;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.testutils.ocpp.exchange.TransactionEvent;
import com.evbox.everon.ocpp.testutils.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutils.constants.StationConstants.*;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionData.ChargingState.CHARGING;
import static com.evbox.everon.ocpp.v20.message.station.TransactionData.ChargingState.EV_DETECTED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.UPDATED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class StartTransactionIdTokenFirstTest extends StationSimulatorSetUp {

    @Test
    void shouldStartChargingWithPreAuthorization() {
        int seqNo = 0;

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(StatusNotification.request(OCCUPIED))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(TransactionEvent.request(STARTED, seqNo, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 1, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID, EV_DETECTED, CABLE_PLUGGED_IN))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 2, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID, CHARGING, CHARGING_STATE_CHANGED))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> stationSimulatorRunner.getStation(STATION_ID).getState().hasAuthorizedToken());

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(DEFAULT_EVSE_ID)).isTrue();
            ocppMockServer.verify();
        });
    }
}
