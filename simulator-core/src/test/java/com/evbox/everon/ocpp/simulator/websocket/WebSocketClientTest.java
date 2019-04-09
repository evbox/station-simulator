package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageInbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketClientTest {

    private long RECONNECT_INTERVAL_MS = 500;

    @Mock
    OkHttpWebSocketClient webSocketClientAdapterMock;
    @Mock
    Station stationMock;

    WebSocketClient client;

    @BeforeEach
    void setUp() {
        client = new WebSocketClient(stationMock, webSocketClientAdapterMock, new WebSocketClientConfiguration(1, RECONNECT_INTERVAL_MS));
    }

    @Test
    void shouldReconnectInCaseOfBrokenConnection() {
        //given
        client.onOpen("connection established");

        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect(isNull());
    }

    @Test
    void shouldReconnectWithGivenIntervalOnSubsequentAttempts() {
        //given
        long startTime = System.currentTimeMillis();
        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect(isNull());
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(RECONNECT_INTERVAL_MS);
    }



}