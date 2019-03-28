package com.evbox.everon.ocpp.functional.availability;

import com.evbox.everon.ocpp.testutil.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.*;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.*;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class HeartbeatTest extends StationSimulatorSetUp {

    @Test
    void shouldAdjustCurrentTimeBasedOnHeartbeatResponse() {

        ZonedDateTime serverTime = ZonedDateTime.of(2035, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);

        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE))
                .thenReturn(emptyResponse());

        ocppMockServer
                .when(heartbeatRequest())
                .thenReturn(heartbeatResponse(serverTime));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            Instant timeOfStation = stationSimulatorRunner.getStation(STATION_ID).getState().getCurrentTime();
            assertThat(timeOfStation).isAfterOrEqualTo(serverTime.toInstant());
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSetHeartbeatWithGivenInterval() {
        int expectedHeartbeatInterval = 100;

        mockBootResponses();

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval()).isEqualTo(expectedHeartbeatInterval);
            ocppMockServer.verify();
        });

    }
}