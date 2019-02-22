package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import lombok.Value;

/**
 * Contains initial request for variable modification and result of its validation.
 */
@Value
public class SetVariableValidationResult {

    private SetVariableDatum setVariableDatum;
    private SetVariableResult result;

    public boolean isAccepted() {
        return result.getAttributeStatus() == SetVariableResult.AttributeStatus.ACCEPTED;
    }
}
