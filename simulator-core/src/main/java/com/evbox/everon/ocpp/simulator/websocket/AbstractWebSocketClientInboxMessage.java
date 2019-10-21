package com.evbox.everon.ocpp.simulator.websocket;

import lombok.ToString;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
public abstract class AbstractWebSocketClientInboxMessage {
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

    public AbstractWebSocketClientInboxMessage() {
        this.sequenceId = SEQUENCE.getAndIncrement();
        this.data = null;
    }

    public AbstractWebSocketClientInboxMessage(Object data) {
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

    public static final class Connect extends AbstractWebSocketClientInboxMessage {
        @Override
        public Type getType() {
            return Type.CONNECT;
        }
    }

    public static final class Disconnect extends AbstractWebSocketClientInboxMessage {
        @Override
        public Type getType() {
            return Type.DISCONNECT;
        }
    }

    public static final class OcppMessage extends AbstractWebSocketClientInboxMessage {

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
