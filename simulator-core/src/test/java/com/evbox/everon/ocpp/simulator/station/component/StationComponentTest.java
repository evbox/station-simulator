package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StationComponentTest {

    private static final List<ReportData> REPORT_DATA = singletonList(new ReportData());

    private static final String COMPONENT_NAME = "testComponent";

    StationComponent stationComponent;

    @Mock
    VariableAccessor mutableAccessorMock;

    @Mock
    VariableAccessor immutableAccessorMock;

    @BeforeEach
    void initComponent() {
        when(mutableAccessorMock.isMutable()).thenReturn(true);
        when(immutableAccessorMock.isMutable()).thenReturn(false);
        when(mutableAccessorMock.getVariableName()).thenReturn("MutableVariable");
        when(immutableAccessorMock.getVariableName()).thenReturn("ImmutableVariable");
        when(mutableAccessorMock.generateReportData(any())).thenReturn(REPORT_DATA);

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
        when(immutableAccessorMock.generateReportData(any())).thenReturn(REPORT_DATA);

        List<ReportData> reportData = stationComponent.generateReportData(false);
        assertThat(reportData.size()).isEqualTo(2);
    }
}
