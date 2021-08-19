package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableStatus;

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
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()));

        if (attributeValue.toString().isEmpty() || TxStartStopPointVariableValues.validateStringOfValues(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableStatus.ACCEPTED);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableStatus.REJECTED); //TODO check that this correlates to INVALID_VALUE in OCPP 2.0
        }
    }

}
