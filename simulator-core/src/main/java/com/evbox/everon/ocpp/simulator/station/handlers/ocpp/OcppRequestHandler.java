package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

/**
 * Handler for request coming from ocpp server.
 *
 * @param <T> request
 */
public interface OcppRequestHandler<T> {

    /**
     * Handle ocpp request from ocpp server.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    void handle(String callId, T request);
}
