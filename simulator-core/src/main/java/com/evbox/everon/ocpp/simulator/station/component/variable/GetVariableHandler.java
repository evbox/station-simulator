package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;

@FunctionalInterface
public interface GetVariableHandler {

    /**
     * Validates {@link GetVariableDatum} for proper variable path (variable name, instance, attributeType, evseId, connectorId) and read access.
     * Retrieves variable from station.
     *
     * @param getVariableDatum contains necessary data to get variable from station
     * @return result of getting variable
     */
    GetVariableResult getVariable(GetVariableDatum getVariableDatum);

}
