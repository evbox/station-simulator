package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCallResult;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createGetVariablesRequest;
import static com.evbox.everon.ocpp.simulator.support.OcppMessageFactory.createGetVariablesResponse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_HEARTBEAT_INTERVAL;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetVariablesRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationComponentsHolder componentsHolderMock;
    @Mock
    OCPPCommCtrlrComponent commCtrlrComponentMock;
    @Captor
    ArgumentCaptor<WebSocketClientInboxMessage> messageCaptor;
    @InjectMocks
    GetVariablesRequestHandler getVariablesRequestHandler;

    @Test
    void verifyCallResult() throws JsonProcessingException {
        //given
        initOCPPCommCtrlComponentMock(HeartbeatIntervalVariableAccessor.NAME, String.valueOf(DEFAULT_HEARTBEAT_INTERVAL));

        GetVariablesRequest getVariablesRequest = createGetVariablesRequest()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .build();

        //when
        getVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, getVariablesRequest);

        //then
        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        GetVariablesResponse getVariablesResponse = createGetVariablesResponse()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withAttributeValue(String.valueOf(DEFAULT_HEARTBEAT_INTERVAL))
                .build();

        String expectedCallResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(getVariablesResponse)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResultJson);

    }

    private void initOCPPCommCtrlComponentMock(String variableName, String variableValue) {
        given(componentsHolderMock.getComponent(OCPPCommCtrlrComponent.NAME)).willReturn(Optional.of(commCtrlrComponentMock));
        given(commCtrlrComponentMock.handle(any(GetVariableDatum.class))).willAnswer(invocation -> new GetVariableResult()
                .withComponent(new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME)))
                .withVariable(new Variable().withName(new CiString.CiString50(variableName)))
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withAttributeValue(new CiString.CiString1000(variableValue))
        );
    }
}
