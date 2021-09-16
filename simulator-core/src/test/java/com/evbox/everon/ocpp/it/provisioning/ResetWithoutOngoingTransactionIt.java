package com.evbox.everon.ocpp.it.provisioning;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.BootNotification;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.centralserver.Reset;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetRequest;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v201.message.station.BootReason.REMOTE_RESET;
import static org.awaitility.Awaitility.await;

public class ResetWithoutOngoingTransactionIt extends StationSimulatorSetUp {

    @Test
    void shouldImmediatelyResetWithoutOngoingTransaction() {

        stationBootCount = times(2);

        ocppMockServer
                .when(BootNotification.request(REMOTE_RESET))
                .thenReturn(BootNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        Call call = new Call(DEFAULT_CALL_ID, ActionType.RESET, new ResetRequest().withType(Reset.IMMEDIATE));

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}
