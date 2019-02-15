package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.StationMessageInbox;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import static com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage.Type.*;

/**
 * Serves as a proxy between station and WebSocket connection.
 * Handles reconnection logic in case of connection failure.
 */
public class WebSocketClient implements WebSocketClientAdapter.ChannelListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

    private final WebSocketClientAdapter webSocketClientAdapter;

    private final PriorityBlockingQueue<WebSocketClientInboxMessage> inbox;
    private final WebSocketClientConfiguration configuration;
    private final Executor executor;
    private final WebSocketMesageSender mesageSender;
    private final StationMessageInbox stationMessageInbox;

    private volatile boolean connected = false;

    private volatile String webSocketConnectionUrl;

    private final ImmutableMap<WebSocketClientInboxMessage.Type, Consumer<WebSocketClientInboxMessage>> messageHandlers = ImmutableMap.<WebSocketClientInboxMessage.Type,
            Consumer<WebSocketClientInboxMessage>>builder()
            .put(OCPP_MESSAGE, (message) -> onOcppMessage((WebSocketClientInboxMessage.OcppMessage) message))
            .put(CONNECT, (message) -> onConnect((WebSocketClientInboxMessage.Connect) message))
            .put(DISCONNECT, (message) -> onDisconnect((WebSocketClientInboxMessage.Disconnect) message))
            .build();

    private final String stationId;

    public WebSocketClient(StationMessageInbox stationMessageInbox, String stationId, WebSocketClientAdapter webSocketClientAdapter) {
        this(stationMessageInbox, stationId, webSocketClientAdapter, WebSocketClientConfiguration.DEFAULT_CONFIGURATION);
    }

    public WebSocketClient(StationMessageInbox stationMessageInbox, String stationId, WebSocketClientAdapter webSocketClientAdapter, WebSocketClientConfiguration configuration) {
        this.stationMessageInbox = stationMessageInbox;
        this.stationId = stationId;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("message-offer-worker-" + stationId).build());

        this.webSocketClientAdapter = webSocketClientAdapter;

        this.configuration = configuration;
        this.mesageSender = new WebSocketMesageSender(webSocketClientAdapter, configuration.getMaxSendAttempts());
        this.inbox = new PriorityBlockingQueue<>(1,
                Comparator.comparing(WebSocketClientInboxMessage::getPriority).thenComparing(WebSocketClientInboxMessage::getSequenceId));
        webSocketClientAdapter.setListener(this);
    }

    public void startAcceptingMessages() {

        executor.execute(() -> {
            for (;;) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                processMessage();
            }
        });
    }

    public BlockingQueue<WebSocketClientInboxMessage> getInbox() {
        return inbox;
    }

    @VisibleForTesting
    public void processMessage() {
        try {
            WebSocketClientInboxMessage message = inbox.take();
            messageHandlers.get(message.getType()).accept(message);
        } catch (InterruptedException e) {
            LOGGER.error("Exception on processing message from WebSocketInbox", e);
            Thread.currentThread().interrupt();
        }
    }

    private void onOcppMessage(WebSocketClientInboxMessage.OcppMessage message) {
        if (connected) {
            String ocppMessage = (String) message.getData().orElseThrow(() -> new IllegalArgumentException("OCPP message is null"));
            mesageSender.send(ocppMessage);
            LOGGER.info("SENT:\n{}", ocppMessage);
        } else {
            inbox.put(message);
        }
    }

    private void onConnect(WebSocketClientInboxMessage.Connect message) {
        webSocketClientAdapter.connect(webSocketConnectionUrl);

    }

    private void onDisconnect(WebSocketClientInboxMessage.Disconnect message) {
        webSocketClientAdapter.disconnect();
        connected = false;
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
        LOGGER.info("RECEIVED:\n{}", message);
        stationMessageInbox.offer(new StationMessage(stationId, StationMessage.Type.OCPP_MESSAGE, message));
    }

    /**
     * Method is called by 3rd-party WebSocket client thread
     * @param message
     */
    @Override
    public void onFailure(Throwable throwable, String message) {
        LOGGER.error(message, throwable);

        if (throwable instanceof IOException) {

            if (connected) {
                connected = false;
            } else {
                LOGGER.error("Connection is broken, will try to reconnect in {} ms...", configuration.getReconnectIntervalMs());
                try {
                    Thread.sleep(configuration.getReconnectIntervalMs());
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            webSocketClientAdapter.connect(webSocketConnectionUrl);
        }
    }

    public void connect(String webSocketUrl) {

        this.webSocketConnectionUrl = webSocketUrl;

        getInbox().add(new WebSocketClientInboxMessage.Connect());
    }
}
