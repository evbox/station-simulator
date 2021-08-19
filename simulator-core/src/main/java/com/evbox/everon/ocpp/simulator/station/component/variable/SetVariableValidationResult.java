package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableData;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableStatus;
import lombok.Value;

/**
 * Contains initial request for variable modification and result of its validation.
 */
@Value
public class SetVariableValidationResult {

    private SetVariableData setVariableData;
    private SetVariableResult result;

    public boolean isAccepted() {
        return result.getAttributeStatus() == SetVariableStatus.ACCEPTED;
    }
}
