package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.StationMessageInbox;
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

    private final StationMessageInbox stationMessageInbox;
    private final String stationId;
    private final OkHttpWebSocketClient webSocketClientAdapter;
    private final WebSocketClientConfiguration configuration;
    private final WebSocketMessageSender messageSender;
    private final WebSocketMessageInbox webSocketMessageInbox;

    private volatile String webSocketConnectionUrl;

    public WebSocketClient(StationMessageInbox stationMessageInbox, String stationId, OkHttpWebSocketClient webSocketClientAdapter) {
        this(stationMessageInbox, stationId, webSocketClientAdapter, WebSocketClientConfiguration.DEFAULT_CONFIGURATION);
    }

    public WebSocketClient(StationMessageInbox stationMessageInbox, String stationId, OkHttpWebSocketClient webSocketClientAdapter, WebSocketClientConfiguration configuration) {
        this.stationMessageInbox = stationMessageInbox;
        this.stationId = stationId;
        this.webSocketClientAdapter = webSocketClientAdapter;
        this.configuration = configuration;

        this.messageSender = new WebSocketMessageSender(webSocketClientAdapter, configuration.getSendRetryIntervalMs(), configuration.getMaxSendAttempts());
        this.webSocketMessageInbox = new WebSocketMessageInbox();

        webSocketClientAdapter.setListener(this);
    }

    public void connect(String webSocketUrl) {

        this.webSocketConnectionUrl = webSocketUrl;

        getInbox().offer(new AbstractWebSocketClientInboxMessage.Connect());
    }

    public void reconnect() {
        webSocketClientAdapter.reconnect(webSocketConnectionUrl);
    }

    public void disconnect() {
        webSocketClientAdapter.disconnect();
    }

    public void startAcceptingMessages() {

        WebSocketMessageRouter webSocketMessageRouter = new WebSocketMessageRouter(this);

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("websocket-message-consumer-" + stationId).build();
        WebSocketMessageConsumer.runSingleThreaded(webSocketMessageInbox, webSocketMessageRouter, threadFactory);
    }

    public WebSocketMessageInbox getInbox() {
        return webSocketMessageInbox;
    }

    public WebSocketMessageSender getMessageSender() {
        return messageSender;
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
    public void onMessage(String message) {
        log.info("RECEIVED: {}", message);
        stationMessageInbox.offer(new StationMessage(stationId, StationMessage.Type.OCPP_MESSAGE, message));
    }

    /**
     * Method is called by 3rd-party WebSocket client thread
     * @param message
     */
    @Override
    public void onFailure(Throwable throwable, String message) {
        log.error(message, throwable);

        if (throwable instanceof IOException) {

            log.error("Connection is broken, will try to reconnect in {} ms...", configuration.getReconnectIntervalMs());
            try {
                Thread.sleep(configuration.getReconnectIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error(e.getMessage(), e);
            }

            webSocketClientAdapter.connect(webSocketConnectionUrl);
        }
    }

}
