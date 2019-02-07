package com.evbox.everon.ocpp.simulator.message;

import java.util.Arrays;

public enum MessageType {

    CALL(2, "Request message"),
    CALL_RESULT(3, "Response message"),
    CALL_ERROR(4, "Error response to a request message");

    private final int id;
    private final String description;

    MessageType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static MessageType of(int id) {
        return Arrays.stream(values())
                .filter(type -> type.getId() == id)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Incorrect message type ID: " + id));
    }
}
