package com.evbox.everon.ocpp.simulator.station.handlers;

/**
 * Handler contract. Used for handling user and server incoming messages.
 *
 * @param <T> message
 */
public interface MessageHandler<T> {

    /**
     * Handle an incoming message.
     *
     * @param message an incoming message
     */
    void handle(T message);
}
