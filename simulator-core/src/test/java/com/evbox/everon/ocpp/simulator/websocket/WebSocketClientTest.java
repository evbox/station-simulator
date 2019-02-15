package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.StationMessageInbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;

import static com.evbox.everon.ocpp.simulator.support.StationConstants.STATION_ID;
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
    WebSocketClientAdapter webSocketClientAdapterMock;

    @Captor
    ArgumentCaptor<String> messageCaptor;

    WebSocketClient client;

    @BeforeEach
    void setUp() {
        client = new WebSocketClient(new StationMessageInbox(), STATION_ID, webSocketClientAdapterMock, new WebSocketClientConfiguration(1, RECONNECT_INTERVAL_MS));
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

    @Test
    void shouldGiveHigherPriorityToConnectMessage() throws Exception {
        //given
        client.onOpen("connection established");
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));
        client.getInbox().put(new WebSocketClientInboxMessage.Connect());
        client.getInbox().put(new WebSocketClientInboxMessage.OcppMessage("{}"));

        //when
        client.processMessage();

        //then
        verify(webSocketClientAdapterMock).connect(isNull());
    }

    @Test
    void shouldGiveHigherPriorityToDisconnectMessage() throws Exception {
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
    void shouldGivePriorityBasedOnMessageSequenceId() throws Exception {
        given(webSocketClientAdapterMock.sendMessage(any())).willReturn(true);

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