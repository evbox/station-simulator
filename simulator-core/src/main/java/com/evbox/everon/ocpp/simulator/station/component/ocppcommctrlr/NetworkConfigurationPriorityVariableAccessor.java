package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.evbox.everon.ocpp.v201.message.station.Component;
import com.evbox.everon.ocpp.v201.message.station.Variable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.v201.message.station.Data.SEQUENCE_LIST;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class NetworkConfigurationPriorityVariableAccessor extends VariableAccessor {

    public static final String NAME = "NetworkConfigurationPriority";

    NetworkConfigurationPriorityVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
    }

    private final Map<AttributeType, VariableGetter> variableGetters = Map.of(AttributeType.ACTUAL, this::getActualValue);

    private final Map<AttributeType, SetVariableValidator> variableValidators = Map.of(AttributeType.ACTUAL, this::validateActualValue);

    private final Map<AttributeType, VariableSetter> variableSetters = Map.of(AttributeType.ACTUAL, this::setActualValue);

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
        return variableSetters;
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportData> generateReportData(String componentName) {
        Component component = new Component()
                .withName(new CiString.CiString50(componentName));

        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(String.valueOf(getStationStore().getNetworkConfigurationPriority())))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(SEQUENCE_LIST)
                .withSupportsMonitoring(false);

        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()));

        String[] values = attributeValue.toString().split(",");
        if(Arrays.stream(values).parallel().anyMatch(value -> !isNumeric(value))){
            return setVariableResult.withAttributeStatus(SetVariableStatus.REJECTED);
        }

        if(Arrays.stream(values).parallel().anyMatch(value -> !getStationStore().getNetworkConnectionProfiles().containsKey(Integer.parseInt(value)))){
            return setVariableResult.withAttributeStatus(SetVariableStatus.REJECTED);
        }

        return setVariableResult.withAttributeStatus(SetVariableStatus.ACCEPTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {

        List<Integer> networkNetworkConfigurationPriority = getStationStore().getNetworkConfigurationPriority();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatus.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(networkNetworkConfigurationPriority)));
    }

    private void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        getStation().updateNetworkConfigurationPriorityValues(Integer.parseInt(attributeValue.toString()));
    }
}
