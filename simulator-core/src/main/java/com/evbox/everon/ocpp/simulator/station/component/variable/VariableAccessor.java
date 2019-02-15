package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.simulator.station.Station;

import java.util.Collections;
import java.util.Set;

public abstract class VariableAccessor implements VariableGetter, VariableSetter {

    private final Station station;

    public VariableAccessor(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    public abstract String getVariableName();

    public Set<String> getSupportedAttributeTypes() {
        return Collections.singleton(AttributeTypes.ACTUAL);
    }

    public boolean isSupported(String attributeType) {
        return getSupportedAttributeTypes().contains(attributeType);
    }
}
