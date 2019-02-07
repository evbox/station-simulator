package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.ConnectException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketClientTest {

    private long RECONNECT_INTERVAL_MS = 500;

    @Mock
    private WebSocketClientAdapter webSocketClientAdapterMock;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private WebSocketClient client;

    @Before
    public void setUp() {
        client = new WebSocketClient(Runnable::run, webSocketClientAdapterMock, new LinkedBlockingQueue<>(), new WebSocketClientConfiguration(1, RECONNECT_INTERVAL_MS));
        given(webSocketClientAdapterMock.sendMessage(any())).willReturn(true);
    }

    @Test
    public void shouldReconnectInCaseOfBrokenConnection() {
        //given
        client.onOpen("connection established");

        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect();
    }

    @Test
    public void shouldReconnectWithGivenIntervalOnSubsequentAttempts() {
        //given
        long startTime = System.currentTimeMillis();
        //when
        client.onFailure(new ConnectException(), "Unexpected failure");

        //then
        verify(webSocketClientAdapterMock).connect();
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(RECONNECT_INTERVAL_MS);
    }

    @Test
    public void shouldGiveHigherPriorityToConnectMessage() throws Exception {
        //given
        client.onOpen("connection established");
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));
        client.getInbox().put(new WebSocketClientInboxMessage.Connect());
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));

        //when
        client.processMessage();

        //then
        verify(webSocketClientAdapterMock).connect();
    }

    @Test
    public void shouldGiveHigherPriorityToDisconnectMessage() throws Exception {
        //given
        client.onOpen("connection established");
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));
        client.getInbox().put(new WebSocketClientInboxMessage.Disconnect());
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));

        //when
        client.processMessage();

        //then
        verify(webSocketClientAdapterMock).disconnect();
    }

    @Test
    public void shouldGivePriorityBasedOnMessageSequenceId() throws Exception {
        //given
        client.onOpen("connection established");

        WebSocketClientInboxMessage.OcppMessage message1 = new WebSocketClientInboxMessage.OcppMessage("1");
        WebSocketClientInboxMessage.OcppMessage message2 = new WebSocketClientInboxMessage.OcppMessage("2");
        WebSocketClientInboxMessage.OcppMessage message3 = new WebSocketClientInboxMessage.OcppMessage("3");

        client.getInbox().put(message2);
        client.getInbox().put(message3);
        client.getInbox().put(message1);

        //when
        client.processMessage();
        client.processMessage();
        client.processMessage();

        //then
        verify(webSocketClientAdapterMock, times(3)).sendMessage(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues()).containsExactly("1", "2", "3");
    }
}