package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.StationMessageInbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketClientTest {

    private final long RECONNECT_INTERVAL_MS = 500;

    @Mock
    OkHttpWebSocketClient webSocketClientAdapterMock;
    @Mock
    StationMessageInbox stationMessageInboxMock;

    WebSocketClient client;

    @BeforeEach
    void setUp() {
        client = new WebSocketClient(stationMessageInboxMock, STATION_ID, webSocketClientAdapterMock, new WebSocketClientConfiguration(1, 0L, RECONNECT_INTERVAL_MS));
    }

    @Test
    void shouldReconnectInCaseOfBrokenConnection() {
        //given
        client.onOpen("connection established");

        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect(anyString());
    }

    @Test
    void shouldReconnectWithGivenIntervalOnSubsequentAttempts() {
        //given
        long startTime = System.currentTimeMillis();
        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect(anyString());
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(RECONNECT_INTERVAL_MS);
    }
}