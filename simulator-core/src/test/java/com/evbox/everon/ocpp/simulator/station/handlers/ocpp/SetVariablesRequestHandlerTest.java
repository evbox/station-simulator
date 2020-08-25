package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_HEARTBEAT_INTERVAL;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory.createCallResult;
import static com.evbox.everon.ocpp.mock.factory.OcppMessageFactory.createSetVariablesRequest;
import static com.evbox.everon.ocpp.mock.factory.OcppMessageFactory.createSetVariablesResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetVariablesRequestHandlerTest {

    private static final String UNKNOWN_COMPONENT_NAME = "UnknownComponent";

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationComponentsHolder componentsHolder;
    @Mock
    OCPPCommCtrlrComponent ocppCommCtrlrComponentMock;
    @Captor
    ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor;
    @InjectMocks
    SetVariablesRequestHandler setVariablesRequestHandler;

    @Test
    void shouldHandleVariable() throws JsonProcessingException {
        //given
        initOCPPCommCtrlComponentMock(SetVariableStatus.ACCEPTED);

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeValue(String.valueOf(DEFAULT_HEARTBEAT_INTERVAL))
                .build();

        //when
        setVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, setVariablesRequest);

        //then
        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        SetVariablesResponse setVariablesResponse = createSetVariablesResponse()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeStatus(SetVariableStatus.ACCEPTED)
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(setVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);
    }

    @Test
    void shouldFailIfComponentNotExists() throws Exception {
        //given
        given(componentsHolder.getComponent(new CiString.CiString50(UNKNOWN_COMPONENT_NAME))).willReturn(Optional.empty());

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest()
                .withComponent(UNKNOWN_COMPONENT_NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .build();

        //when
        setVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, setVariablesRequest);

        //then
        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        SetVariablesResponse setVariablesResponse = createSetVariablesResponse()
                .withComponent(UNKNOWN_COMPONENT_NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeStatus(SetVariableStatus.UNKNOWN_COMPONENT)
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(setVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);
    }

    @Test
    void shouldFailIfValidationFailed() throws Exception {
        //given
        initOCPPCommCtrlComponentMock(SetVariableStatus.REJECTED);

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .build();

        //when
        setVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, setVariablesRequest);

        //then
        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        SetVariablesResponse setVariablesResponse = createSetVariablesResponse()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeStatus(SetVariableStatus.REJECTED)
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(setVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);
    }

    private void initOCPPCommCtrlComponentMock(SetVariableStatus attributeStatus) {
        given(componentsHolder.getComponent(new CiString.CiString50(OCPPCommCtrlrComponent.NAME))).willReturn(Optional.of(ocppCommCtrlrComponentMock));
        given(ocppCommCtrlrComponentMock.validate(any()))
                .willAnswer(invocation -> {
                    SetVariableData data = invocation.getArgument(0);
                    SetVariableResult setVariableResult = new SetVariableResult()
                            .withComponent(data.getComponent())
                            .withVariable(data.getVariable())
                            .withAttributeType(Attribute.fromValue(data.getAttributeType().value()))
                            .withAttributeStatus(attributeStatus);
                    return new SetVariableValidationResult(data, setVariableResult);
                });
    }

}
