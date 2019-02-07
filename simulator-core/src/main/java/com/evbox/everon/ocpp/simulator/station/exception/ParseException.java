package com.evbox.everon.ocpp.simulator.station.exception;

public class ParseException extends RuntimeException {
    public ParseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
