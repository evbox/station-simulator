package com.evbox.everon.ocpp.simulator.websocket;

public interface ChannelListener {


    default void onOpen(String response) {
    }

    default void onMessage(String message) {
    }

    default void onFailure(Throwable throwable, String message) {
    }

    default void onClosing(int code, String reason) {
    }

    default void onClosed(int code, String reason) {
    }

}
