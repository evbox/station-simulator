package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v201.message.centralserver.Component;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v201.message.centralserver.SetVariableStatus;
import com.evbox.everon.ocpp.v201.message.centralserver.Variable;
import com.evbox.everon.ocpp.v201.message.station.NetworkConnectionProfile;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.VariableConstants.COMM_COMPONENT_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.NETWORK_CONFIGURATION_PRIORITY_VARIABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkConfigurationPriorityVariableAccessorTest {

    private static final List<Integer> PRIORITY_LIST = Arrays.asList(1, 2, 3);
    private static final CiString.CiString2500 NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST = new CiString.CiString2500(String.valueOf(PRIORITY_LIST));
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

        verifyNoInteractions(station);
        verifyNoInteractions(stationStore);
    }

    @Test
    void getVariableGetters() {
        when(stationStore.getNetworkConfigurationPriority()).thenReturn(PRIORITY_LIST);

        final VariableGetter variableGetter = variableAccessor.getVariableGetters().get(AttributeType.ACTUAL);
        assertThat(variableGetter.get(attributePath()).getAttributeValue()).isEqualTo(NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST);

        verifyNoInteractions(station);
        verify(stationStore).getNetworkConfigurationPriority();
    }

    @Test
    void getVariableSetters() {
        final VariableSetter variableSetter = variableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        verify(station).updateNetworkConfigurationPriorityValues(argThat(arg -> arg == 1));
        verifyNoInteractions(stationStore);
    }

    @Test
    void getVariableValidatorsValidValue() {
        Map<Integer, NetworkConnectionProfile> networkConnectionProfiles = Map.of(1, new NetworkConnectionProfile(), 2, new NetworkConnectionProfile());

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        final SetVariableResult result = variableAccessor.validate(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.ACCEPTED);

        verify(stationStore).getNetworkConnectionProfiles();
        verifyNoInteractions(station);
    }

    @Test
    void getVariableValidatorsInvalidValue() {
        Map<Integer, NetworkConnectionProfile> networkConnectionProfiles = Map.of(3, new NetworkConnectionProfile(), 4, new NetworkConnectionProfile());

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        final SetVariableResult result = variableAccessor.validate(attributePath(), NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_SINGLE);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.REJECTED);

        verify(stationStore).getNetworkConnectionProfiles();
        verifyNoInteractions(station);
    }

    @Test
    void getVariableValidatorsInvalidValueNan() {
        Map<Integer, NetworkConnectionProfile> networkConnectionProfiles = Map.of(3, new NetworkConnectionProfile(), 4, new NetworkConnectionProfile());

        when(stationStore.getNetworkConnectionProfiles()).thenReturn(networkConnectionProfiles);

        SetVariableResult result = variableAccessor.validate(attributePath(), new CiString.CiString1000("asd"));

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatus.REJECTED);

        verifyNoInteractions(stationStore);
        verifyNoInteractions(station);
    }

    @Test
    void generateReportData() {
        when(stationStore.getNetworkConfigurationPriority()).thenReturn(PRIORITY_LIST);

        final List<ReportData> reportData = variableAccessor.generateReportData(COMM_COMPONENT_NAME);

        assertThat(NETWORK_CONFIGURATION_PRIORITY_ATTRIBUTE_LIST).isEqualTo(reportData.get(0).getVariableAttribute().get(0).getValue());

        verify(stationStore).getNetworkConfigurationPriority();
        verifyNoInteractions(station);
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
