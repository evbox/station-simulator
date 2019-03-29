package com.evbox.everon.ocpp.functional.provisioning;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutil.station.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.bootNotificationRequest;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.bootNotificationResponseMock;
import static com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest.Reason.REMOTE_RESET;
import static org.awaitility.Awaitility.await;

public class ResetWithoutOngoingTransaction extends StationSimulatorSetUp {

    @Test
    void shouldImmediatelyResetWithoutOngoingTransaction() {

        ocppMockServer
                .when(bootNotificationRequest(), times(2))
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer.expectRequestFromStation(bootNotificationRequest(REMOTE_RESET));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        Call call = new Call(UUID.randomUUID().toString(), ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE));

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
