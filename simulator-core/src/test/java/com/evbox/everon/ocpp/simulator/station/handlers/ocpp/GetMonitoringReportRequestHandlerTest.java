package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMonitoringReportRequestHandlerTest {

    @Mock
    StationComponent stationComponent;
    @Mock
    StationComponentsHolder stationComponentsHolder;
    @Mock
    StationMessageSender stationMessageSender;
    @InjectMocks
    GetMonitoringReportRequestHandler handler;

    @Captor
    ArgumentCaptor<GetMonitoringReportResponse> responseCaptor = ArgumentCaptor.forClass(GetMonitoringReportResponse.class);
    @Captor
    ArgumentCaptor<Map<ComponentVariable, List<SetMonitoringDatum>>> resultsCaptor = ArgumentCaptor.forClass(Map.class);

    @Test
    void verifyNotSupportedResponse() {
        GetMonitoringReportRequest request = new GetMonitoringReportRequest()
                                                            .withRequestId(1)
                                                            .withMonitoringCriteria(Collections.singletonList(MonitoringCriterium.PERIODIC_MONITORING));

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.NOT_SUPPORTED);
    }

    @Test
    void verifyAcceptedNoCriteriaEmpty() {
        when(stationComponentsHolder.getAllMonitoredComponents()).thenReturn(new HashMap<>());
        GetMonitoringReportRequest request = new GetMonitoringReportRequest().withRequestId(1);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.REJECTED);

        verify(stationMessageSender, never()).sendNotifyMonitoringReport(anyInt(), any());
    }

    @Test
    void verifyAcceptedStatusCriteriaEmpty() {
        when(stationComponentsHolder.getAllMonitoredComponents()).thenReturn(new HashMap<>());
        GetMonitoringReportRequest request = new GetMonitoringReportRequest()
                                                        .withRequestId(1)
                                                        .withMonitoringCriteria(Collections.singletonList(MonitoringCriterium.THRESHOLD_MONITORING));

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.REJECTED);

        verify(stationMessageSender, never()).sendNotifyMonitoringReport(anyInt(), any());
    }

    @Test
    void verifyAcceptedComponentVariableCriteriaEmpty() {
        ComponentVariable cv = getComponentVariable("component", "variable");
        GetMonitoringReportRequest request = new GetMonitoringReportRequest()
                                                        .withRequestId(1)
                                                        .withComponentVariable(Collections.singletonList(cv));

        when(stationComponentsHolder.getByComponentAndVariable(Collections.singletonList(cv))).thenReturn(new HashMap<>());

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.REJECTED);

        verify(stationMessageSender, never()).sendNotifyMonitoringReport(anyInt(), any());
    }

    @Test
    void verifyCorrectNumberOfReports() {
        final int size = 5;
        Map<ComponentVariable, List<SetMonitoringDatum>> map =  new HashMap<>();
        ComponentVariable cv = getComponentVariable("component", "variable");
        List<SetMonitoringDatum> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(generateDatum(i, SetMonitoringDatum.Type.LOWER_THRESHOLD, cv.getComponent(), cv.getVariable()));
        }
        map.put(cv, list);
        when(stationComponentsHolder.getAllMonitoredComponents()).thenReturn(map);

        GetMonitoringReportRequest request = new GetMonitoringReportRequest().withRequestId(1);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.ACCEPTED);

        verify(stationMessageSender).sendNotifyMonitoringReport(eq(1), resultsCaptor.capture());
        Map<ComponentVariable, List<SetMonitoringDatum>> result = resultsCaptor.getValue();
        assertThat(result.get(cv)).hasSize(size);
    }

    @Test
    void verifyCorrectWithCriteria() {
        final int size = 5;
        Map<ComponentVariable, List<SetMonitoringDatum>> map =  new HashMap<>();
        ComponentVariable cv = getComponentVariable("component", "variable");
        List<SetMonitoringDatum> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(generateDatum(i, SetMonitoringDatum.Type.LOWER_THRESHOLD, cv.getComponent(), cv.getVariable()));
        }
        map.put(cv, list);
        when(stationComponentsHolder.getAllMonitoredComponents()).thenReturn(map);

        GetMonitoringReportRequest request = new GetMonitoringReportRequest()
                                                    .withRequestId(1)
                                                    .withMonitoringCriteria(Collections.singletonList(MonitoringCriterium.DELTA_MONITORING));

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.REJECTED);

        verify(stationMessageSender, never()).sendNotifyMonitoringReport(anyInt(), any());
    }

    @Test
    void verifyCorrectWithMixedCriteria() {
        final int size = 5;
        Map<ComponentVariable, List<SetMonitoringDatum>> map =  new HashMap<>();
        ComponentVariable cv = getComponentVariable("component", "variable");
        ComponentVariable cv2 = getComponentVariable("component2", "variable2");
        List<SetMonitoringDatum> list = new ArrayList<>();
        List<SetMonitoringDatum> list2 = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(generateDatum(i, SetMonitoringDatum.Type.LOWER_THRESHOLD, cv.getComponent(), cv.getVariable()));
            list2.add(generateDatum(i, SetMonitoringDatum.Type.DELTA, cv.getComponent(), cv.getVariable()));
        }
        map.put(cv, list);
        map.put(cv2, list2);
        when(stationComponentsHolder.getByComponentAndVariable(Collections.singletonList(cv2))).thenReturn(map);

        GetMonitoringReportRequest request = new GetMonitoringReportRequest()
                                                    .withRequestId(1)
                                                    .withComponentVariable(Collections.singletonList(cv2))
                                                    .withMonitoringCriteria(Collections.singletonList(MonitoringCriterium.DELTA_MONITORING));

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        GetMonitoringReportResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(GetMonitoringReportResponse.Status.ACCEPTED);

        verify(stationMessageSender).sendNotifyMonitoringReport(eq(1), resultsCaptor.capture());
        Map<ComponentVariable, List<SetMonitoringDatum>> result = resultsCaptor.getValue();
        assertThat(result.get(cv2)).hasSize(size);
    }

    private ComponentVariable getComponentVariable(String componentName, String variableName) {
        return new ComponentVariable()
                    .withComponent(new Component().withName(new CiString.CiString50(componentName)))
                    .withVariable(new Variable().withName(new CiString.CiString50(variableName)));
    }

    private SetMonitoringDatum generateDatum(int id, SetMonitoringDatum.Type type, Component component, Variable variable) {
        return new SetMonitoringDatum()
                .withId(id)
                .withComponent(component)
                .withVariable(variable)
                .withSeverity(1)
                .withTransaction(false)
                .withValue(BigDecimal.ONE)
                .withType(type);
    }

}
