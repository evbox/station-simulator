package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v20.message.centralserver.*;

public class HeartbeatIntervalVariableAccessor extends VariableAccessor {

    public static final String NAME = "HeartbeatInterval";

    public HeartbeatIntervalVariableAccessor(Station station) {
        super(station);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public GetVariableResult get(Component component, Evse evse, Variable variable, GetVariableDatum.AttributeType attributeType) {
        Station station = getStation();
        int heartbeatInterval = station.getState().getHeartbeatInterval();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withComponent(component)
                .withVariable(variable)
                .withAttributeType(GetVariableResult.AttributeType.fromValue(attributeType.value()))
                .withAttributeValue(new CiString.CiString1000(String.valueOf(heartbeatInterval)));
    }

    @Override
    public void set(Component component, Evse evse, Variable variable, SetVariableDatum.AttributeType attributeType, CiString.CiString1000 attributeValue) {
        Station station = getStation();

        int newHeartbeatInterval = Integer.valueOf(attributeValue.toString());
        station.updateHeartbeat(newHeartbeatInterval);
    }
}
