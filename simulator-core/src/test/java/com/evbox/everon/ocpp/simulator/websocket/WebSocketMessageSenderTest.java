package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketMessageSenderTest {

    private static int MAX_SEND_ATTEMPTS = 5;

    @Mock
    WebSocketClientAdapter webSocketClientAdapterMock;

    WebSocketMesageSender mesageSender;

    @BeforeEach
    void setUp() {
        mesageSender = new WebSocketMesageSender(webSocketClientAdapterMock, MAX_SEND_ATTEMPTS);
    }

    @Test
    void shouldRetrySendingMessages() {
        //given
        given(webSocketClientAdapterMock.sendMessage(any())).willReturn(false);


        //when
        mesageSender.send("hello");

        //then
        verify(webSocketClientAdapterMock, times(MAX_SEND_ATTEMPTS)).sendMessage(any());
    }

}