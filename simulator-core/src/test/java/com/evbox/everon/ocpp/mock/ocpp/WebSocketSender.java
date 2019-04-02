package com.evbox.everon.ocpp.mock.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple class that sends a message via webSocketChannel.
 */
@Slf4j
@AllArgsConstructor
public class WebSocketSender {

    private final WebSocketChannel webSocketChannel;
    private final RequestResponseSynchronizer requestResponseSynchronizer;

    /**
     * Send a message without waiting for the response.
     *
     * @param message to the station
     */
    public void sendMessage(String message) {
        WebSockets.sendText(message, webSocketChannel, null);
    }

    /**
     * Send a message to the station and wait for the response.
     *
     * @param message to the station
     * @param clazz   serialize incoming response to the specified class
     * @param <T>     type of the response
     * @return an instance of the specified class
     */
    public <T> T sendMessage(String message, Class<T> clazz) {
        WebSockets.sendText(message, webSocketChannel, null);

        CallResult stationResponse = requestResponseSynchronizer.take();

        return stationResponse.getPayload(clazz);
    }
}
