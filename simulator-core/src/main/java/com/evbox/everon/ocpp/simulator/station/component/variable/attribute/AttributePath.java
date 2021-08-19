package com.evbox.everon.ocpp.simulator.station.component.variable.attribute;

import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.Component;
import com.evbox.everon.ocpp.v201.message.centralserver.Variable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Contains all data necessary to access arbitrary variable's attribute of the station
 */
@Value
@Builder
@AllArgsConstructor
public class AttributePath {

    /**
     * Contains name of component, component's instance name, EVSE ID, Connector ID
     */
    private Component component;

    /**
     * Contains name of variable, variable's instance name
     */
    private Variable variable;

    /**
     * Contains type of variable's attribute, such as Actual, MinSet, MaxSet, Target
     */
    private AttributeType attributeType;

    public AttributePath(Component component, Variable variable, Attribute attributeType) {
        this.component = component;
        this.variable = variable;
        this.attributeType = AttributeType.from(attributeType);
    }

}
