package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.mock.ocpp.exchange.BootNotification;
import com.evbox.everon.ocpp.mock.station.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest.Reason.REMOTE_RESET;
import static org.awaitility.Awaitility.await;

public class ResetWithoutOngoingTransactionIt extends StationSimulatorSetUp {

    @Test
    void shouldImmediatelyResetWithoutOngoingTransaction() {

        ocppMockServer
                .when(BootNotification.request(), times(2))
                .thenReturn(BootNotification.response());

        ocppMockServer
                .when(BootNotification.request(REMOTE_RESET))
                .thenReturn(BootNotification.response());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        Call call = new Call(UUID.randomUUID().toString(), ActionType.RESET, new ResetRequest().withType(ResetRequest.Type.IMMEDIATE));

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
