package com.evbox.everon.ocpp.it.authorization;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.v201.message.station.ChargingState.CHARGING;
import static com.evbox.everon.ocpp.v201.message.station.ChargingState.EV_CONNECTED;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.OCCUPIED;
import static com.evbox.everon.ocpp.v201.message.station.IdTokenType.ISO_14443;
import static com.evbox.everon.ocpp.v201.message.station.TransactionEvent.STARTED;
import static com.evbox.everon.ocpp.v201.message.station.TransactionEvent.UPDATED;
import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.CABLE_PLUGGED_IN;
import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.CHARGING_STATE_CHANGED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class EvDriverAuthorizationUsingRfidIt extends StationSimulatorSetUp {

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

        //TODO Check replacement of EV_DETECTED with ChargingState.EV_CONNECTED which is the closest match,
        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 1, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID, EV_CONNECTED, CABLE_PLUGGED_IN))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(UPDATED, seqNo + 2, DEFAULT_TRANSACTION_ID, DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID, CHARGING, CHARGING_STATE_CHANGED))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> stationSimulatorRunner.getStation(STATION_ID).getStateView().hasAuthorizedToken());

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue();
            ocppMockServer.verify();
        });
    }

}
