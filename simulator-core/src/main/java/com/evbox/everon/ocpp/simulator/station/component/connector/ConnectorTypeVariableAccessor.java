package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.evbox.everon.ocpp.v20.message.station.VariableAttribute;
import com.evbox.everon.ocpp.v20.message.station.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.station.VariableCharacteristics.DataType.OPTION_LIST;
import static java.util.Collections.singletonList;

public class ConnectorTypeVariableAccessor extends VariableAccessor {

    public static final String NAME = "ConnectorType";
    public static final String CONNECTOR_TYPE = "CHAdeMO";

    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    public ConnectorTypeVariableAccessor(Station station) {
        super(station);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<AttributeType, VariableGetter> getVariableGetters() {
        return variableGetters;
    }

    @Override
    public Map<AttributeType, VariableSetter> getVariableSetters() {
        return Collections.emptyMap();
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportDatum> generateReportData(String componentName) {
        List<ReportDatum> reportData = new ArrayList<>();

        for (Evse evse: getStation().getState().getEvses()) {
            for (Connector connector: evse.getConnectors()) {
                com.evbox.everon.ocpp.v20.message.common.Evse componentEvse = new com.evbox.everon.ocpp.v20.message.common.Evse()
                        .withConnectorId(connector.getId())
                        .withId(evse.getId());

                Component component = new Component()
                        .withName(new CiString.CiString50(componentName))
                        .withEvse(componentEvse);

                VariableAttribute variableAttribute = new VariableAttribute()
                        .withValue(new CiString.CiString1000(CONNECTOR_TYPE));

                ReportDatum reportDatum = new ReportDatum()
                        .withComponent(component)
                        .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                        .withVariableCharacteristics(new VariableCharacteristics().withDataType(OPTION_LIST))
                        .withVariableAttribute(singletonList(variableAttribute));

                reportData.add(reportDatum);
            }
        }

        return reportData;
    }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return RESULT_CREATOR.createResult(attributePath, attributeValue, SetVariableResult.AttributeStatus.REJECTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();
        Integer connectorId = attributePath.getComponent().getEvse().getConnectorId();

        boolean connectorExists = getStation().getState().tryFindConnector(evseId, connectorId).isPresent();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(GetVariableResult.AttributeType.fromValue(attributePath.getAttributeType().getName()));

        if (!connectorExists) {
            return getVariableResult
                    .withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED);
        } else {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString1000(CONNECTOR_TYPE))
                    .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED);
        }
    }
}
