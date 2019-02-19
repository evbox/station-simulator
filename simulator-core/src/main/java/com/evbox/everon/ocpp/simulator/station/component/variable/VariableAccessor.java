package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;

public abstract class VariableAccessor implements VariableGetter, VariableSetter {

    private final Station station;

    public VariableAccessor(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    public abstract String getVariableName();

    public abstract SetVariableResult.AttributeStatus validate(SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue);

}
