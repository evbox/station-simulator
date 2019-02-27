package com.evbox.everon.ocpp.simulator.station.component.exception;

public class IllegalAttributeTypeException extends RuntimeException {

    public IllegalAttributeTypeException(String value) {
        super("Unknown attribute type: " + value);
    }

}
