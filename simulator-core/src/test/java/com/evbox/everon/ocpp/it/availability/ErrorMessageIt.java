package com.evbox.everon.ocpp.it.availability;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.NotifyEvent;
import com.evbox.everon.ocpp.simulator.station.actions.user.Fault;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static org.awaitility.Awaitility.await;

class ErrorMessageIt extends StationSimulatorSetUp {
    @Test
    void shouldSendNotifyEventWhenStationFaulted() {
        int evseId = 1;
        int connectorId = 2;
        String errorCode = "0x0102";
        String errorDescription = "Auto-recoverable DC Leakage detected";

        ocppMockServer
                .when(NotifyEvent.request(evseId, connectorId, errorCode, errorDescription))
                .thenReturn(NotifyEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Fault(evseId, connectorId, errorCode, errorDescription));

        await().untilAsserted(() -> ocppMockServer.verify());
    }
}
