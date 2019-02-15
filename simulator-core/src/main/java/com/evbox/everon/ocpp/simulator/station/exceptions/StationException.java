package com.evbox.everon.ocpp.simulator.station.exceptions;

public class StationException extends RuntimeException {

    public StationException() {
        super();
    }

    public StationException(String message) {
        super(message);
    }

    public StationException(String message, Throwable cause) {
        super(message, cause);
    }
}
