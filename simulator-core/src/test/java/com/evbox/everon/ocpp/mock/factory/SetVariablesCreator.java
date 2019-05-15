package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;

import static java.util.Collections.singletonList;

public class SetVariablesCreator {

    public static SetVariablesRequest createSetVariablesRequest(String component, String variable, String value, SetVariableDatum.AttributeType type) {
        SetVariableDatum setVariableDatum = new SetVariableDatum()
                .withComponent(new Component().withName(new CiString.CiString50(component)))
                .withVariable(new Variable().withName(new CiString.CiString50(variable)))
                .withAttributeType(type)
                .withAttributeValue(new CiString.CiString1000(value));

        return new SetVariablesRequest().withSetVariableData(singletonList(setVariableDatum));
    }
}
