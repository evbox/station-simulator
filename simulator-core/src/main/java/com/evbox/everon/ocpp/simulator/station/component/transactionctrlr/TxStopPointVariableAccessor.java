package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.GetVariableStatus;
import com.evbox.everon.ocpp.v201.message.station.*;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v201.message.station.Data.OPTION_LIST;
import static java.util.Collections.singletonList;

public class TxStopPointVariableAccessor extends VariableAccessor {

    public static final String NAME = "TxStopPoints";
    private final Map<AttributeType, VariableGetter> variableGetters =  Map.of(AttributeType.ACTUAL, this::getActualValue);

    private final Map<AttributeType, SetVariableValidator> variableValidators =  Map.of(AttributeType.ACTUAL, TxStartStopPointUtils::validateActualValue);

    private final Map<AttributeType, VariableSetter> variableSetters =  Map.of(AttributeType.ACTUAL, this::setActualValue);

    public TxStopPointVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
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
                .withValue(new CiString.CiString2500(String.valueOf(getStationStore().getTxStopPointValues())))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(OPTION_LIST)
                .withSupportsMonitoring(false);

        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }

    @Override
    public boolean isMutable() { return true; }

    public void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        String[] values = attributeValue.toString().split(",");
        List<TxStartStopPointVariableValues> stopPoints = new ArrayList<>();
        for (String value : values) {
            stopPoints.add(TxStartStopPointVariableValues.fromValue(value));
        }

        Station station = getStation();
        station.updateTxStopPointValues(new OptionList<>(stopPoints));
        getStationStore().setTxStopPointValues(new OptionList<>(stopPoints));
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatus.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(Attribute.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(getStationStore().getTxStopPointValues())));
    }
}
