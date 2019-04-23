package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketMessageSenderTest {

    private static final int SEND_RETRY_INTERVAL_MS = 0;
    private static int MAX_SEND_ATTEMPTS = 5;

    @Mock
    OkHttpWebSocketClient webSocketClientAdapterMock;

    WebSocketMessageSender mesageSender;

    @BeforeEach
    void setUp() {
        mesageSender = new WebSocketMessageSender(webSocketClientAdapterMock, SEND_RETRY_INTERVAL_MS, MAX_SEND_ATTEMPTS);
    }

    @Test
    void shouldRetrySendingMessages() {
        //given
        given(webSocketClientAdapterMock.sendMessage(any()))
                .willReturn(false)
                .willReturn(false)
                .willReturn(true);


        //when
        mesageSender.send("hello");

        //then
        verify(webSocketClientAdapterMock, times(3)).sendMessage(contains("hello"));
    }

}