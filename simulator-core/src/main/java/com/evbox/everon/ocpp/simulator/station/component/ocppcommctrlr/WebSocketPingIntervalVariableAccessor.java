package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v20.message.centralserver.*;

public class WebSocketPingIntervalVariableAccessor extends VariableAccessor  {

    public static final String NAME = "WebSocketPingInterval";

    public WebSocketPingIntervalVariableAccessor(Station station) {
        super(station);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public GetVariableResult get(Component component, Evse evse, Variable variable, GetVariableDatum.AttributeType attributeType) {
        return null;
    }

    @Override
    public void set(Component component, Evse evse, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
    }
}
