package com.evbox.everon.ocpp.simulator.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage.Type.*;
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

        WebSocketClientInboxMessage.OcppMessage message1 = new WebSocketClientInboxMessage.OcppMessage("1");
        WebSocketClientInboxMessage.OcppMessage message2 = new WebSocketClientInboxMessage.OcppMessage("2");
        WebSocketClientInboxMessage.OcppMessage message3 = new WebSocketClientInboxMessage.OcppMessage("3");

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

        inbox.offer(new WebSocketClientInboxMessage.OcppMessage("{}"));
        inbox.offer(new WebSocketClientInboxMessage.Connect());
        inbox.offer(new WebSocketClientInboxMessage.OcppMessage("{}"));

        assertAll(
                () -> assertThat(inbox.take().getPriority()).isEqualTo(CONNECT.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority())
        );

    }

    @Test
    void shouldGiveHigherPriorityToDisconnectMessage() {

        inbox.offer(new WebSocketClientInboxMessage.OcppMessage("{}"));
        inbox.offer(new WebSocketClientInboxMessage.Disconnect());
        inbox.offer(new WebSocketClientInboxMessage.OcppMessage("{}"));

        assertAll(
                () -> assertThat(inbox.take().getPriority()).isEqualTo(DISCONNECT.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority()),
                () -> assertThat(inbox.take().getPriority()).isEqualTo(OCPP_MESSAGE.getPriority())
        );
    }
}
