package com.evbox.everon.ocpp.testutils;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.AllArgsConstructor;

/**
 * A simple class that sends a message via webSocketChannel.
 */
@AllArgsConstructor
public class WebSocketSender {

    private final WebSocketChannel webSocketChannel;

    public void sendMessage(String message) {
        WebSockets.sendText(message, webSocketChannel, null);
    }
}
