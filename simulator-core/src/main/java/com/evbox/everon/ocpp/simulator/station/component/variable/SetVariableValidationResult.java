package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import lombok.Value;

@Value
public class SetVariableValidationResult {

    private SetVariableDatum setVariableDatum;
    private SetVariableResult.AttributeStatus status;

    public boolean isAccepted() {
        return status == SetVariableResult.AttributeStatus.ACCEPTED;
    }
}
