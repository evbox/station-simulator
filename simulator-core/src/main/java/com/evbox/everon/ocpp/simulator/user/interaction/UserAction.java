package com.evbox.everon.ocpp.simulator.user.interaction;

import lombok.Value;

public abstract class UserAction {

    public enum Type {
        PLUG, UNPLUG, AUTHORIZE
    }

    public abstract Type getType();

    @Value
    public static class Plug extends UserAction {
        Integer connectorId;

        @Override
        public Type getType() {
            return Type.PLUG;
        }
    }

    @Value
    public static class Unplug extends UserAction {
        Integer connectorId;

        @Override
        public Type getType() {
            return Type.UNPLUG;
        }
    }

    @Value
    public static class Authorize extends UserAction {
        String tokenId;
        Integer evseId;

        @Override
        public Type getType() {
            return Type.AUTHORIZE;
        }
    }
}
