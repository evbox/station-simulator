package com.evbox.everon.ocpp.simulator.station;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HeartbeatSenderTaskTest {
    private static final int HEARTBEAT_INTERVAL = 30;

    @Mock
    StationStore stationStore;

    @Mock
    StationMessageSender stationMessageSender;

    HeartbeatSenderTask task;

    @BeforeEach
    void setUp() {
        this.task = new HeartbeatSenderTask(stationStore, stationMessageSender);
        task.updateHeartBeatInterval(HEARTBEAT_INTERVAL);
    }

    @Test
    @DisplayName("Heartbeat sender should send heartbeat request on startup regardless of time")
    void shouldSendHeartBeatOnStartup() {
        when(stationMessageSender.getTimeOfLastMessageSent()).thenReturn(LocalDateTime.MIN);

        task.run();

        verify(stationMessageSender, times(1)).sendHeartBeatAndSubscribe(any(), any());
    }

    @Test
    @DisplayName("Heartbeat sender should not send more than one heartbeat in 30s")
    void shouldNotSendMoreThanOneHeartbeat() {
        when(stationMessageSender.getTimeOfLastMessageSent()).thenReturn(LocalDateTime.now());

        for (int i = 0; i < 3; i++) {
            task.run();
        }

        verify(stationMessageSender, times(1)).sendHeartBeatAndSubscribe(any(), any());
    }

    @Test
    @DisplayName("Heartbeat sender should send heartbeat because no message was sent in the last 30s")
    void shouldSendHeartbeatBecauseOfInactivity() {
        when(stationMessageSender.getTimeOfLastMessageSent())
                .thenReturn(LocalDateTime.now().minusSeconds(HEARTBEAT_INTERVAL + 1));

        // Initial run will always execute
        task.run();

        // Second run will execute because last message was now() - 31s ago
        task.run();

        verify(stationMessageSender, times(2)).sendHeartBeatAndSubscribe(any(), any());
    }
}
