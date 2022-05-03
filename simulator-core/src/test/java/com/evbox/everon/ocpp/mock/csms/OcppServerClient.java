package com.evbox.everon.ocpp.mock.csms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A client that holds websocket senders.
 *
 * A websocket sender can be retrieved by station_id.
 */
public class OcppServerClient {

    // maps station_id -> websocket sender
    private final Map<String, WebSocketSender> webSocketChannelMap = new ConcurrentHashMap<>();
    private AtomicBoolean connected = new AtomicBoolean();

    /**
     * Puts a new entry to the map. If exists then return the associated websocket sender.
     *
     * @param stationId station identity
     * @param webSocketSender {@link WebSocketSender} instance
     * @return
     */
    public WebSocketSender putIfAbsent(String stationId, WebSocketSender webSocketSender) {
        return webSocketChannelMap.putIfAbsent(stationId, webSocketSender);
    }

    /**
     * Check whether server is connected to the station.
     *
     * @return true if connected otherwise false
     */
    public boolean isConnected() {
        return connected.get();
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
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
