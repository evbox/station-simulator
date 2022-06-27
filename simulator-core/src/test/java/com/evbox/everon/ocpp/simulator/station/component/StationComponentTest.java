package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StationComponentTest {

    private static final List<ReportData> REPORT_DATA = singletonList(new ReportData());

    private static final String COMPONENT_NAME = "testComponent";

    StationComponent stationComponent;

    VariableAccessor mutableAccessorMock;

    VariableAccessor immutableAccessorMock;

    @BeforeEach
    void initComponent() {
        mutableAccessorMock = createVariableAccessor(true, "MutableVariable");
        immutableAccessorMock = createVariableAccessor(false, "ImmutableVariable");

        stationComponent = new StationComponent(asList(mutableAccessorMock, immutableAccessorMock)) {
            @Override
            public String getComponentName() {
                return COMPONENT_NAME;
            }
        };
    }

    @Test
    @DisplayName("Generating base reports for mutable variables only")
    void shouldGenerateBaseReportForMutableVariablesOnly() {
        List<ReportData> reportData = stationComponent.generateReportData(true);
        assertThat(reportData.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Generating base reports for all variables")
    void shouldGenerateBaseReportForAllVariables() {
        List<ReportData> reportData = stationComponent.generateReportData(false);
        assertThat(reportData.size()).isEqualTo(2);
    }


    private VariableAccessor createVariableAccessor(boolean mutable, String variableName) {
        return new VariableAccessor(null, null) {
            @Override
            public String getVariableName() {
                return variableName;
            }

            @Override
            public Map<AttributeType, VariableGetter> getVariableGetters() {
                return null;
            }

            @Override
            public Map<AttributeType, VariableSetter> getVariableSetters() {
                return null;
            }

            @Override
            public Map<AttributeType, SetVariableValidator> getVariableValidators() {
                return null;
            }

            @Override
            public List<ReportData> generateReportData(String componentName) {
                return REPORT_DATA;
            }

            @Override
            public boolean isMutable() {
                return mutable;
            }
        };
    }
}
