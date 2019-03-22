package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
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

    private static final List<ReportDatum> REPORT_DATUM = singletonList(new ReportDatum());

    private static final String COMPONENT_NAME = "testComponent";

    private StationComponent stationComponent;

    @Mock
    VariableAccessor mutableAccessor;

    @Mock
    VariableAccessor immutableAccessor;

    @BeforeEach
    void initComponent() {
        when(mutableAccessor.isMutable()).thenReturn(true);
        when(immutableAccessor.isMutable()).thenReturn(false);
        when(mutableAccessor.getVariableName()).thenReturn("MutableVariable");
        when(immutableAccessor.getVariableName()).thenReturn("ImmutableVariable");
        when(mutableAccessor.generateReportData(any())).thenReturn(REPORT_DATUM);

        stationComponent = new StationComponent(asList(mutableAccessor, immutableAccessor)) {
            @Override
            public String getComponentName() {
                return COMPONENT_NAME;
            }
        };
    }

    @Test
    @DisplayName("Generating base reports for mutable variables only")
    void shouldGenerateBaseReportForMutableVariablesOnly() {
        List<ReportDatum> reportData = stationComponent.generateReportData(true);
        assertThat(reportData.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Generating base reports for all variables")
    void shouldGenerateBaseReportForAllVariables() {
        when(immutableAccessor.generateReportData(any())).thenReturn(REPORT_DATUM);

        List<ReportDatum> reportData = stationComponent.generateReportData(false);
        assertThat(reportData.size()).isEqualTo(2);
    }
}
