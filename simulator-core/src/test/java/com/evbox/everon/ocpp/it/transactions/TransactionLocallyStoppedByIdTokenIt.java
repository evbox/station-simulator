package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.mock.ocpp.exchange.Authorize;
import com.evbox.everon.ocpp.mock.ocpp.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.ocpp.exchange.TransactionEvent;
import com.evbox.everon.ocpp.mock.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TransactionLocallyStoppedByIdTokenIt extends StationSimulatorSetUp {

    @Test
    void shouldStopChargingOnSecondAuth() {
        int seqNo = 0;

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), times(2))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(StatusNotification.request(OCCUPIED))
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

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isFalse();
            ocppMockServer.verify();
        });
    }
}
