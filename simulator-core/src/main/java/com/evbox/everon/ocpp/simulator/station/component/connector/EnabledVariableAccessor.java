package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;

import java.util.Map;

public class EnabledVariableAccessor extends VariableAccessor {

    public static final String NAME = "Enabled";

    public EnabledVariableAccessor(Station station) {
        super(station);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<GetVariableDatum.AttributeType, VariableGetter> getVariableGetters() {
        return null;
    }

    @Override
    public Map<SetVariableDatum.AttributeType, VariableSetter> getVariableSetters() {
        return null;
    }

    @Override
    public Map<SetVariableDatum.AttributeType, SetVariableValidator> getVariableValidators() {
        return null;
    }
}
