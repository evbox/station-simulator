package com.evbox.everon.ocpp.simulator.station.exceptions;

public class ParseException extends RuntimeException {
    public ParseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
