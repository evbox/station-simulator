package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.BootNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest.Reason.REMOTE_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ResetWithOngoingTransactionIt extends StationSimulatorSetUp {

    @Test
    void shouldImmediatelyResetWithOngoingTransaction() {

        stationBootCount = times(2);

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(BootNotification.request(REMOTE_RESET), atLeastOnce())
                .thenReturn(BootNotification.response());

        ocppMockServer
                .when(TransactionEvent.request(), atLeastOnce())
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(1)).isTrue());

        Call call = new Call(DEFAULT_CALL_ID, ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE));

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(1)).isFalse();
            ocppMockServer.verify();
        });
    }
}
