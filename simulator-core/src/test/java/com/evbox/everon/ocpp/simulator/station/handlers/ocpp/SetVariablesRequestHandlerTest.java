package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCallResult;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createSetVariablesRequest;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createSetVariablesResponse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetVariablesRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @InjectMocks
    SetVariablesRequestHandler setVariablesRequestHandler;

    @Test
    void shouldCheckIfComponentExists() throws JsonProcessingException {
        SetVariablesRequest setVariablesRequest = createSetVariablesRequest()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .build();

        setVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, setVariablesRequest);

        ArgumentCaptor<WebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        SetVariablesResponse setVariablesResponse = createSetVariablesResponse()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .withAttributeStatus(SetVariableResult.AttributeStatus.REJECTED)
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(setVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);
    }

    @Test
    void shouldPassVariableForValidation() {
        fail();
    }

    @Test
    void shouldSendResponseWithValidationStatus() {
        fail();
    }

    @Test
    void shouldApplyAcceptedVariables() {
        fail();
    }
}
