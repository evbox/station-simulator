package com.evbox.everon.ocpp.simulator.configuration.exception;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException() {
    }

    public ConfigurationException(Throwable t) {
        super(t);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable t) {
        super(message, t);
    }
}
