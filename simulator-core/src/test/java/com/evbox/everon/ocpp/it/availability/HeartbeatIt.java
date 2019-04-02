package com.evbox.everon.ocpp.it.availability;

import com.evbox.everon.ocpp.mock.ocpp.exchange.Heartbeat;
import com.evbox.everon.ocpp.mock.station.StationSimulatorSetUp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.atLeastOnce;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class HeartbeatIt extends StationSimulatorSetUp {

    private static final ZonedDateTime SERVER_TIME = ZonedDateTime.of(2035, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);

    @Test
    void shouldAdjustCurrentTimeBasedOnHeartbeatResponse() {

        ocppMockServer
                .when(Heartbeat.request(), atLeastOnce())
                .thenReturn(Heartbeat.response(SERVER_TIME));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            Instant timeOfStation = stationSimulatorRunner.getStation(STATION_ID).getState().getCurrentTime();
            assertThat(timeOfStation).isAfterOrEqualTo(SERVER_TIME.toInstant());
            ocppMockServer.verify();
        });
    }

    @Test
    void shouldSendHeartbeatWithGivenInterval() {
        int expectedHeartbeatInterval = 100;

        ocppMockServer
                .when(Heartbeat.request(), atLeastOnce())
                .thenReturn(Heartbeat.response(SERVER_TIME));

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getState().getHeartbeatInterval()).isEqualTo(expectedHeartbeatInterval);
            ocppMockServer.verify();
        });
    }
}