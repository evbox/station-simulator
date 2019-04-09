package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;

/**
 * Represents websocket communication between station and CSMS.
 *
 * Provides reconnection logic in case of connection failure.
 */
@Slf4j
public class WebSocketClient implements ChannelListener {

    private final Station station;
    private final OkHttpWebSocketClient webSocketClientAdapter;
    private final WebSocketClientConfiguration configuration;
    private final WebSocketMessageSender messageSender;
    private final WebSocketMessageInbox webSocketMessageInbox;

    private volatile boolean connected = false;
    private volatile String webSocketConnectionUrl;

    public WebSocketClient(Station station, OkHttpWebSocketClient webSocketClientAdapter) {
        this(station, webSocketClientAdapter, WebSocketClientConfiguration.DEFAULT_CONFIGURATION);
    }

    public WebSocketClient(Station station, OkHttpWebSocketClient webSocketClientAdapter, WebSocketClientConfiguration configuration) {
        this.station = station;
        this.webSocketClientAdapter = webSocketClientAdapter;
        this.configuration = configuration;

        this.messageSender = new WebSocketMessageSender(webSocketClientAdapter, configuration.getMaxSendAttempts());
        this.webSocketMessageInbox = new WebSocketMessageInbox();

        webSocketClientAdapter.setListener(this);
    }

    public void connect(String webSocketUrl) {

        this.webSocketConnectionUrl = webSocketUrl;

        getInbox().offer(new WebSocketClientInboxMessage.Connect());
    }

    public void startAcceptingMessages() {

        WebSocketMessageRouter webSocketMessageRouter = new WebSocketMessageRouter(this);

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("websocket-message-consumer-" + station.getId()).build();
        WebSocketMessageConsumer.runSingleThreaded(webSocketMessageInbox, webSocketMessageRouter, threadFactory);
    }

    public WebSocketMessageInbox getInbox() {
        return webSocketMessageInbox;
    }

    public WebSocketMessageSender getMessageSender() {
        return messageSender;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getWebSocketConnectionUrl() {
        return webSocketConnectionUrl;
    }

    public OkHttpWebSocketClient getWebSocketClientAdapter() {
        return webSocketClientAdapter;
    }

    /**
     * Method is called by 3rd-party WebSocket client thread
     * @param message
     */
    @Override
    public void onOpen(String message) {
        connected = true;
    }

    /**
     * Method is called by 3rd-party WebSocket client thread
     * @param message
     */
    @Override
    public void onMessage(String message) {
        log.info("RECEIVED: {}", message);
        station.sendMessage(new StationMessage(station.getId(), StationMessage.Type.OCPP_MESSAGE, message));
    }

    /**
     * Method is called by 3rd-party WebSocket client thread
     * @param message
     */
    @Override
    public void onFailure(Throwable throwable, String message) {
        log.error(message, throwable);

        if (throwable instanceof IOException) {

            if (connected) {
                connected = false;
            } else {
                log.error("Connection is broken, will try to reconnect in {} ms...", configuration.getReconnectIntervalMs());
                try {
                    Thread.sleep(configuration.getReconnectIntervalMs());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }

            webSocketClientAdapter.connect(webSocketConnectionUrl);
        }
    }

}
