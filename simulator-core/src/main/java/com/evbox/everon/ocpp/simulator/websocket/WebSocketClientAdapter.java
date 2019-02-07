package com.evbox.everon.ocpp.simulator.websocket;

import java.util.Optional;

public abstract class WebSocketClientAdapter {

    private Optional<ChannelListener> listener;

    public void onOpen(String response) {
        listener.ifPresent(s -> s.onOpen(response));
    }

    public void onMessage(String message) {
        listener.ifPresent(s -> s.onMessage(message));
    }

    public void onFailure(Throwable throwable, String message) {
        listener.ifPresent(s -> s.onFailure(throwable, message));
    }

    public void onClosing(int code, String reason) {
        listener.ifPresent(s -> s.onClosing(code, reason));
    }

    public void onClosed(int code, String reason) {
        listener.ifPresent(s -> s.onClosed(code, reason));
    }

    public void setListener(ChannelListener listener) {
        this.listener = Optional.of(listener);
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract boolean sendMessage(String message);

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
}
