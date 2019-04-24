package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.Unplug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WhenCableDisconnectedOnEvSideStopTransactionIt extends StationSimulatorSetUp {

    @Test
    void shouldEndOngoingTransactionOnSecondAuth() {
        int seqNo = 0;

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), times(2))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(StatusNotification.request(), atLeastOnce())
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(TransactionEvent.request(STARTED, seqNo, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 1, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 2, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(ENDED, seqNo + 3, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(1)).isTrue());

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        triggerUserAction(STATION_ID, new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).hasTokenId()).isFalse();
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isFalse();
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldPreserveTransactionIdPerEvseForWholeSession() {
        int seqNo = 0;

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), times(2))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(StatusNotification.request(), atLeastOnce())
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(TransactionEvent.request(STARTED, seqNo, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(ENDED, seqNo + 3, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        triggerUserAction(STATION_ID, new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
