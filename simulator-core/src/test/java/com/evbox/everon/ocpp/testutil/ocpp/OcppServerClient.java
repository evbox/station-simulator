package com.evbox.everon.ocpp.testutil.ocpp;

import com.evbox.everon.ocpp.testutil.WebSocketSender;
import io.undertow.websockets.core.WebSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A client that holds websocket senders.
 *
 * A websocket sender can be retrieved by station_id.
 */
public class OcppServerClient {

    // maps station_id -> websocket sender
    private final Map<String, WebSocketSender> webSocketChannelMap = new ConcurrentHashMap<>();
    private volatile boolean connected;

    /**
     * Puts a new entry to the map. If exists then return the associated websocket sender.
     *
     * @param stationId station identity
     * @param webSocketChannel {@link WebSocketChannel} instance
     * @return
     */
    public WebSocketSender putIfAbsent(String stationId, WebSocketChannel webSocketChannel) {
        return webSocketChannelMap.putIfAbsent(stationId, new WebSocketSender(webSocketChannel));
    }

    /**
     * Check whether server is connected to the station.
     *
     * @return true if connected otherwise false
     */
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Find {@link WebSocketSender} by specified station_id.
     *
     * @param stationId station identity
     * @return {@link WebSocketSender} instance.
     */
    public WebSocketSender findStationSender(String stationId) {
        return webSocketChannelMap.get(stationId);
    }
}
