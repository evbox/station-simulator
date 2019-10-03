package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;

/**
 * Utils class holds shared methods between txStart and txStop variables
 */
class TxStartStopPointUtils {
    private TxStartStopPointUtils () {
        // NOP
    }

    static SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(SetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));

        if (attributeValue.toString().isEmpty() || TxStartStopPointVariableValues.validateStringOfValues(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.ACCEPTED);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableResult.AttributeStatus.INVALID_VALUE);
        }
    }

}
