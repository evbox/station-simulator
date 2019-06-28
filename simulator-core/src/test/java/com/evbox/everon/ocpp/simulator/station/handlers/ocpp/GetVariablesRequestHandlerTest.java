package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.HeartbeatIntervalVariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
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
import static com.evbox.everon.ocpp.mock.factory.OcppMessageFactory.createGetVariablesRequest;
import static com.evbox.everon.ocpp.mock.factory.OcppMessageFactory.createGetVariablesResponse;
import static com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult.AttributeStatus.UNKNOWN_COMPONENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    ArgumentCaptor<GetVariablesResponse> messageCaptor;
    @InjectMocks
    GetVariablesRequestHandler getVariablesRequestHandler;

    @Test
    void verifyCallResult() {
        //given
        initOCPPCommCtrlComponentMock(HeartbeatIntervalVariableAccessor.NAME, String.valueOf(DEFAULT_HEARTBEAT_INTERVAL));

        GetVariablesRequest getVariablesRequest = createGetVariablesRequest()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .build();

        //when
        getVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, getVariablesRequest);

        //then
        verify(stationMessageSender).sendCallResult(anyString(), messageCaptor.capture());

        GetVariablesResponse getVariablesResponse = createGetVariablesResponse()
                .withComponent(OCPPCommCtrlrComponent.NAME)
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withAttributeValue(String.valueOf(DEFAULT_HEARTBEAT_INTERVAL))
                .build();

        GetVariableResult expectedResult = getVariablesResponse.getGetVariableResult().get(0);

        GetVariableResult actualResult = messageCaptor.getValue().getGetVariableResult().get(0);

        assertAll(
                () -> assertThat(actualResult.getAttributeStatus()).isEqualTo(expectedResult.getAttributeStatus()),
                () -> assertThat(actualResult.getComponent().getName()).isEqualTo(expectedResult.getComponent().getName()),
                () -> assertThat(actualResult.getVariable().getName()).isEqualTo(expectedResult.getVariable().getName()),
                () -> assertThat(actualResult.getAttributeValue()).isEqualTo(expectedResult.getAttributeValue())
        );

    }

    @Test
    void shouldSendUnknownComponent() {
        GetVariablesRequest getVariablesRequest = createGetVariablesRequest()
                .withComponent("unknown component")
                .withVariable(HeartbeatIntervalVariableAccessor.NAME)
                .build();

        //when
        getVariablesRequestHandler.handle(DEFAULT_MESSAGE_ID, getVariablesRequest);

        //then
        verify(stationMessageSender).sendCallResult(anyString(), messageCaptor.capture());

        assertThat(messageCaptor.getValue().getGetVariableResult().get(0).getAttributeStatus()).isEqualTo(UNKNOWN_COMPONENT);
    }

    private void initOCPPCommCtrlComponentMock(String variableName, String variableValue) {
        given(componentsHolderMock.getComponent(new CiString.CiString50(OCPPCommCtrlrComponent.NAME))).willReturn(Optional.of(commCtrlrComponentMock));
        given(commCtrlrComponentMock.getVariable(any(GetVariableDatum.class))).willAnswer(invocation -> new GetVariableResult()
                .withComponent(new Component().withName(new CiString.CiString50(OCPPCommCtrlrComponent.NAME)))
                .withVariable(new Variable().withName(new CiString.CiString50(variableName)))
                .withAttributeStatus(GetVariableResult.AttributeStatus.ACCEPTED)
                .withAttributeValue(new CiString.CiString1000(variableValue))
        );
    }
}
