package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCallResult;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createGetVariablesRequest;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createGetVariablesResponse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GetVariablesRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @InjectMocks
    GetVariablesRequestHandler getVariablesRequestHandler;

    @Test
    void verifyCallResult() throws JsonProcessingException {
        GetVariablesRequest getVariablesRequest = createGetVariablesRequest()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .build();

        getVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, getVariablesRequest);

        ArgumentCaptor<WebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        GetVariablesResponse getVariablesResponse = createGetVariablesResponse()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED)
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(getVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);

    }
}
