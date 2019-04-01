package com.evbox.everon.ocpp.functional.availability;

import com.evbox.everon.ocpp.testutils.ocpp.exchange.Heartbeat;
import com.evbox.everon.ocpp.testutils.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.evbox.everon.ocpp.testutils.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.testutils.expect.ExpectedCount.atLeastOnce;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class HeartbeatTest extends StationSimulatorSetUp {

    @Test
    void shouldAdjustCurrentTimeBasedOnHeartbeatResponse() {

        ZonedDateTime serverTime = ZonedDateTime.of(2035, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);

        ocppMockServer
                .when(Heartbeat.request(), atLeastOnce())
                .thenReturn(Heartbeat.response(serverTime));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            Instant timeOfStation = stationSimulatorRunner.getStation(STATION_ID).getState().getCurrentTime();
            assertThat(timeOfStation).isAfterOrEqualTo(serverTime.toInstant());
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSendHeartbeatWithGivenInterval() {
        int expectedHeartbeatInterval = 100;

        ocppMockServer.expectRequestFromStation(Heartbeat.request());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval()).isEqualTo(expectedHeartbeatInterval);
            ocppMockServer.verify();
        });
    }
}