package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v201.message.station.IdTokenType.ISO_14443;
import static com.evbox.everon.ocpp.v201.message.station.TransactionEvent.STARTED;
import static com.evbox.everon.ocpp.v201.message.station.TransactionEvent.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class TransactionLocallyStoppedByIdTokenIt extends StationSimulatorSetUp {

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
                .when(TransactionEvent.request(STARTED, seqNo, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 1, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 2, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(1)).isTrue());

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(1)).isFalse();
            ocppMockServer.verify();
        });
    }
}
