package com.evbox.everon.ocpp.simulator.station.exceptions;

public class BadServerResponseException extends RuntimeException {
    public BadServerResponseException(String message) {
        super(message);
    }
}
