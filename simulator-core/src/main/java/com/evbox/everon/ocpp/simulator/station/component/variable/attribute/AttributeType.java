package com.evbox.everon.ocpp.simulator.station.component.variable.attribute;

import com.evbox.everon.ocpp.simulator.station.component.exception.IllegalAttributeTypeException;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Internal type to map from SetVariableDatum.AttributeType and GetVariableDatum.AttributeType
 */
public enum AttributeType {

    ACTUAL("Actual"),
    TARGET("Target"),
    MIN_SET("MinSet"),
    MAX_SET("MaxSet");

    private final String type;

    AttributeType(String type) {
        this.type = type;
    }

    public static AttributeType from(Attribute attributeType) {
        return from(attributeType.value()).orElseThrow(() -> new IllegalAttributeTypeException("Unknown attribute type: " + attributeType.value()));
    }

    public static Optional<AttributeType> from(String type) {
        return Stream.of(values())
                .filter(value -> value.getName().equals(type))
                .findAny();
    }

    public String getName() {
        return type;
    }
}
