package com.evbox.everon.ocpp.functional.availability;

import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import com.evbox.everon.ocpp.testutil.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.statusNotificationRequest;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;
import static org.awaitility.Awaitility.await;

public class StatusNotificationTest extends StationSimulatorSetUp {

    @Test
    void shouldSendStatusNotificationRequestWhenChangeOccurs() {

        ocppMockServer.expectRequestFromStation(statusNotificationRequest(OCCUPIED));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            ocppMockServer.verify();
        });
    }
}