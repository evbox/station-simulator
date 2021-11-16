package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import com.evbox.everon.ocpp.v201.message.station.VariableAttribute;
import com.evbox.everon.ocpp.v201.message.station.VariableCharacteristics;

import java.util.List;

import static java.util.Collections.singletonList;

public class ReportDataGenereator {

    public static List<ReportData> generateReportData(String componentName, VariableCharacteristics variableCharacteristics, VariableAttribute variableAttribute, String name){
        com.evbox.everon.ocpp.v201.message.station.Component component = new com.evbox.everon.ocpp.v201.message.station.Component()
                .withName(new CiString.CiString50(componentName));


        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new com.evbox.everon.ocpp.v201.message.station.Variable().withName(new CiString.CiString50(name)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }
}
