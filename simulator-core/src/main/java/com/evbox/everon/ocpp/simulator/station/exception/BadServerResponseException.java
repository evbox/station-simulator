package com.evbox.everon.ocpp.simulator.station.exception;

public class BadServerResponseException extends RuntimeException {
    public BadServerResponseException(String message) {
        super(message);
    }
}
