package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class WebSocketMessageInboxTest {

    WebSocketMessageInbox inbox;

    @BeforeEach
    void setUp() {
        inbox = new WebSocketMessageInbox();
    }

    @Test
    void shouldGivePriorityBasedOnMessageSequenceId() {

        AbstractWebSocketClientInboxMessage.OcppMessageAbstract message1 = new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("1");
        AbstractWebSocketClientInboxMessage.OcppMessageAbstract message2 = new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("2");
        AbstractWebSocketClientInboxMessage.OcppMessageAbstract message3 = new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("3");

        inbox.offer(message2);
        inbox.offer(message3);
        inbox.offer(message1);

        //then
        assertAll(
                () -> assertThat(inbox.take().getData().get()).isEqualTo("1"),
                () -> assertThat(inbox.take().getData().get()).isEqualTo("2"),
                () -> assertThat(inbox.take().getData().get()).isEqualTo("3")
        );
    }

    @Test
    void shouldGiveHigherPriorityToConnectMessage() {

        inbox.offer(new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("{}"));
        inbox.offer(new AbstractWebSocketClientInboxMessage.Connect());
        inbox.offer(new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("{}"));

        assertAll(
                () -> assertThat(inbox.take().getPriority()).isEqualTo(CONNECT.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority())
        );

    }

    @Test
    void shouldGiveHigherPriorityToDisconnectMessage() {

        inbox.offer(new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("{}"));
        inbox.offer(new AbstractWebSocketClientInboxMessage.Disconnect());
        inbox.offer(new AbstractWebSocketClientInboxMessage.OcppMessageAbstract("{}"));

        assertAll(
                () -> assertThat(inbox.take().getPriority()).isEqualTo(DISCONNECT.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority())
        );
    }
}
