package com.evbox.everon.ocpp.simulator.websocket;

import lombok.ToString;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
public abstract class WebSocketClientInboxMessage {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    public enum Type {
        CONNECT(1), DISCONNECT(1), OCPP_MESSAGE(2);

        private final int priority;

        Type(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    final Object data;
    final int sequenceId;

    public WebSocketClientInboxMessage() {
        this.sequenceId = SEQUENCE.getAndIncrement();
        this.data = null;
    }

    public WebSocketClientInboxMessage(Object data) {
        this.sequenceId = SEQUENCE.getAndIncrement();
        this.data = data;
    }

    public abstract Type getType();

    public int getPriority() {
        return getType().getPriority();
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public Optional<Object> getData() {
        return Optional.empty();
    }

    public final static class Connect extends WebSocketClientInboxMessage {
        @Override
        public Type getType() {
            return Type.CONNECT;
        }
    }

    public final static class Disconnect extends WebSocketClientInboxMessage {
        @Override
        public Type getType() {
            return Type.DISCONNECT;
        }
    }

    public final static class OcppMessage extends WebSocketClientInboxMessage {

        public OcppMessage(Object data) {
            super(data);
        }

        @Override
        public Type getType() {
            return Type.OCPP_MESSAGE;
        }

        @Override
        public Optional<Object> getData() {
            return Optional.of(data);
        }
    }
}