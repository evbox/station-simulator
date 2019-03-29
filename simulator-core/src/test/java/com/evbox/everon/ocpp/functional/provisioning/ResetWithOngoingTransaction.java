package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.testutil.station.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.*;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.authorizeResponseMock;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.bootNotificationResponseMock;
import static com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest.Reason.REMOTE_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ResetWithOngoingTransaction extends StationSimulatorSetUp {

    @Test
    void shouldImmediatelyResetWithOngoingTransaction() {

        ocppMockServer
                .when(bootNotificationRequest(), times(2))
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(authorizeRequest())
                .thenReturn(authorizeResponseMock(DEFAULT_TOKEN_ID));

        ocppMockServer
                .expectRequestFromStation(bootNotificationRequest(REMOTE_RESET))
                .expectRequestFromStation(transactionEventRequest(), atLeastOnce());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isTrue());

        Call call = new Call(UUID.randomUUID().toString(), ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE));

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().isCharging(1)).isFalse();
            ocppMockServer.verify();
        });
    }
}
