package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketMessageSenderTest {

    private static int MAX_SEND_ATTEMPTS = 5;

    @Mock
    private WebSocketClientAdapter webSocketClientAdapterMock;

    private WebSocketMesageSender mesageSender;

    @Before
    public void setUp() {
         mesageSender = new WebSocketMesageSender(webSocketClientAdapterMock, MAX_SEND_ATTEMPTS);
    }

    @Test
    public void shouldRetrySendingMessages() {
        //given
        given(webSocketClientAdapterMock.sendMessage(any())).willReturn(false);



        //when
        mesageSender.send("hello");

        //then
        verify(webSocketClientAdapterMock, times(MAX_SEND_ATTEMPTS)).sendMessage(any());
    }

}