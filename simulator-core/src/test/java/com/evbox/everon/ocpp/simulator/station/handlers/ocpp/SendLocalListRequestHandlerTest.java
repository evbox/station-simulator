package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.station.SendLocalListRequest;
import com.evbox.everon.ocpp.v20.message.station.SendLocalListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendLocalListRequestHandlerTest {

    @Mock
    private StationMessageSender stationMessageSender;

    @InjectMocks
    private SendLocalListRequestHandler handler;

    @Captor
    private ArgumentCaptor<SendLocalListResponse> responseCaptor = ArgumentCaptor.forClass(SendLocalListResponse.class);

    @Test
    public void testSendLocalListSuccess() {
        handler.handle("123", new SendLocalListRequest());

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());

        SendLocalListResponse response = responseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(SendLocalListResponse.Status.ACCEPTED);
        assertThat(response.getAdditionalProperties()).isEmpty();

    }

}
