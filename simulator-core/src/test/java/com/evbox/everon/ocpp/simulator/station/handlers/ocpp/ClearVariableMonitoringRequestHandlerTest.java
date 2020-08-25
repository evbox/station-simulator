package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;


import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v201.message.station.ClearMonitoringStatus;
import com.evbox.everon.ocpp.v201.message.station.ClearVariableMonitoringRequest;
import com.evbox.everon.ocpp.v201.message.station.ClearVariableMonitoringResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.evbox.everon.ocpp.v201.message.station.ClearMonitoringStatus.ACCEPTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearVariableMonitoringRequestHandlerTest {

    @Mock
    StationComponentsHolder stationComponentsHolder;
    @Mock
    StationMessageSender stationMessageSender;
    @InjectMocks
    ClearVariableMonitoringRequestHandler handler;

    @Captor
    ArgumentCaptor<ClearVariableMonitoringResponse> responseCaptor = ArgumentCaptor.forClass(ClearVariableMonitoringResponse.class);

    @Test
    void verifyCorrectNumberOfResponses() {
        final int size = 5;
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ids.add(111);
        }
        ClearVariableMonitoringRequest request = new ClearVariableMonitoringRequest().withId(ids);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        ClearVariableMonitoringResponse response = responseCaptor.getValue();
        assertThat(response.getClearMonitoringResult().size()).isEqualTo(size);
        assertTrue(response.getClearMonitoringResult().stream().allMatch(r -> r.getStatus() == ClearMonitoringStatus.NOT_FOUND));
        assertTrue(response.getClearMonitoringResult().stream().allMatch(r -> r.getId() == 111));
    }

    @Test
    void verifyMonitoringIsCleared() {
        final int componentId = 111;
        when(stationComponentsHolder.clearMonitor(componentId)).thenReturn(true);
        ClearVariableMonitoringRequest request = new ClearVariableMonitoringRequest().withId(Collections.singletonList(componentId));

        handler.handle("1", request);

        verify(stationComponentsHolder).clearMonitor(componentId);
        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        ClearVariableMonitoringResponse response = responseCaptor.getValue();
        assertThat(response.getClearMonitoringResult()).hasSize(1);
        assertThat(response.getClearMonitoringResult().get(0).getStatus()).isEqualTo(ACCEPTED);
    }

}
