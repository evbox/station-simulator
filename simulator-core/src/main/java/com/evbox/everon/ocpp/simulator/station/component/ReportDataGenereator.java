package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v201.message.station.*;

import java.util.List;

import static java.util.Collections.singletonList;

public class ReportDataGenereator {

    public static List<ReportData> generateReportData(String componentName, VariableCharacteristics variableCharacteristics, VariableAttribute variableAttribute, String name){
        Component component = new Component()
                .withName(new CiString.CiString50(componentName));


        ReportData reportDatum = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(name)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(reportDatum);
    }
}
