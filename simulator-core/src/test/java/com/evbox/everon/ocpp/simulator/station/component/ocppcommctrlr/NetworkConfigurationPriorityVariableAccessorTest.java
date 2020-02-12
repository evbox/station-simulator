package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;

import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import com.evbox.everon.ocpp.v20.message.station.ConnectionData;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.VariableConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkConfigurationPriorityVariableAccessorTest {

    private static final List<Integer> PRIORITY_LIST = Arrays.asList(1, 2, 3);
    private static final CiString.CiString1000 NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST = new CiString.CiString1000(String.valueOf(PRIORITY_LIST));
    private static final CiString.CiString1000 NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE = new CiString.CiString1000("1");

    @Mock(lenient = true)
    private Station station;

    @Mock(lenient = true)
    private StationStore stationStore;

    @InjectMocks
    private NetworkConfigurationPriorityVariableAccessor variableAccessor;

    @Test
    void getVariableName() {
        final String variableName = variableAccessor.getVariableName();
        assertThat(variableName).isEqualTo(NetworkConfigurationPriorityVariableAccessor.NAME);

        verifyZeroInteractions(station);
        verifyZeroInteractions(stationStore);
    }

    @Test
    void getVariableGetters() {
        when(stationStore.getNetworkConfigurationPriority()).thenReturn(PRIORITY_LIST);

        final VariableGetter variableGetter = variableAccessor.getVariableGetters().get(AttributeType.ACTUAL);
        assertThat(variableGetter.get(attributePath()).getAttributeValue()).isEqualTo(NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST);

        verifyZeroInteractions(station);
        verify(stationStore).getNetworkConfigurationPriority();
    }

    @Test
    void getVariableSetters() {
        final VariableSetter variableSetter = variableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        verify(station).updateNetworkConfigurationPriorityValues(argThat(arg -> arg == 1));
        verifyZeroInteractions(stationStore);
    }

    @Test
    void getVariableValidatorsValidValue() {
        Map<Integer, ConnectionData> networkConnectionProfiles = new HashMap<>() {{
            put(1, new ConnectionData());
            put(2, new ConnectionData());
        }};

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        final SetVariableResult result = variableAccessor.validate(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.ACCEPTED);

        verify(stationStore).getNetworkConnectionProfiles();
        verifyZeroInteractions(station);
    }

    @Test
    void getVariableValidatorsInvalidValue() {
        Map<Integer, ConnectionData> networkConnectionProfiles = new HashMap<>() {{
            put(3, new ConnectionData());
            put(4, new ConnectionData());
        }};

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        final SetVariableResult result = variableAccessor.validate(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.INVALID_VALUE);

        verify(stationStore).getNetworkConnectionProfiles();
        verifyZeroInteractions(station);
    }

    @Test
    void getVariableValidatorsInvalidValueNan() {
        Map<Integer, ConnectionData> networkConnectionProfiles = new HashMap<>() {{
            put(3, new ConnectionData());
            put(4, new ConnectionData());
        }};

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        SetVariableResult result = variableAccessor.validate(attributePath(), new CiString.CiString1000("asd"));

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableResult.AttributeStatus.INVALID_VALUE);

        verifyZeroInteractions(stationStore);
        verifyZeroInteractions(station);
    }

    @Test
    void generateReportData() {
        when(stationStore.getNetworkConfigurationPriority()).thenReturn(PRIORITY_LIST);

        final List<ReportDatum> reportData = variableAccessor.generateReportData(COMM_COMPONENT_NAME);

        assertThat(NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST).isEqualTo(reportData.get(0).getVariableAttribute().get(0).getValue());

        verify(stationStore).getNetworkConfigurationPriority();
        verifyZeroInteractions(station);
    }

    @Test
    void isMutable() {
        Boolean isMutable = variableAccessor.isMutable();

        assertThat(isMutable).isTrue();
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMM_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(NETWORK_CONFIGURATION_PRIORITY_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}