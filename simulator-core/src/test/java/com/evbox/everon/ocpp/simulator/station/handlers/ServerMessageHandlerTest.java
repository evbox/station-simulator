package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.exceptions.BadServerResponseException;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.*;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationResponse;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Map;

import static com.evbox.everon.ocpp.testutil.ReflectionUtils.injectMock;
import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory.*;
import static com.evbox.everon.ocpp.testutil.factory.OcppMessageFactory.*;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerMessageHandlerTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    SubscriptionRegistry subscriptionRegistryMock;

    @Mock
    GetVariablesRequestHandler getVariablesRequestHandlerMock;
    @Mock
    SetVariablesRequestHandler setVariablesRequestHandlerMock;
    @Mock
    ResetRequestHandler resetRequestHandlerMock;
    @Mock
    ChangeAvailabilityRequestHandler changeAvailabilityRequestHandlerMock;
    @Mock
    Station stationMock;

    ServerMessageHandler serverMessageHandler;

    @BeforeEach
    void setUp() {
        serverMessageHandler = new ServerMessageHandler(stationMock, stationStateMock, stationMessageSenderMock, STATION_ID, subscriptionRegistryMock);

        Map<Class, OcppRequestHandler> requestHandlers = ImmutableMap.<Class, OcppRequestHandler>builder()
                .put(GetVariablesRequest.class, getVariablesRequestHandlerMock)
                .put(SetVariablesRequest.class, setVariablesRequestHandlerMock)
                .put(ResetRequest.class, resetRequestHandlerMock)
                .put(ChangeAvailabilityRequest.class, changeAvailabilityRequestHandlerMock)
                .build();

        injectMock(serverMessageHandler, "requestHandlers", requestHandlers);
    }

    @Test
    void verifyCallMessageWithGetVariablesPayload() throws JsonProcessingException {

        GetVariablesRequest payload = createGetVariablesRequest()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .build();

        String callJson = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(GET_VARIABLES_ACTION)
                .withPayload(payload)
                .toJson();

        serverMessageHandler.handle(callJson);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GetVariablesRequest> requestCaptor = ArgumentCaptor.forClass(GetVariablesRequest.class);

        verify(getVariablesRequestHandlerMock).handle(messageIdCaptor.capture(), requestCaptor.capture());

        assertAll(
                () -> assertThat(messageIdCaptor.getValue()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(requestCaptor.getValue()).isInstanceOf(GetVariablesRequest.class)
        );

        CiString.CiString50 actualComponentName = requestCaptor.getValue().getGetVariableData().get(0).getComponent().getName();
        CiString.CiString50 actualVariableName = requestCaptor.getValue().getGetVariableData().get(0).getVariable().getName();

        assertAll(
                () -> assertThat(actualComponentName.toString()).isEqualTo(DEFAULT_COMPONENT_NAME),
                () -> assertThat(actualVariableName.toString()).isEqualTo(DEFAULT_VARIABLE_NAME)
        );

    }

    @Test
    void verifyCallMessageWithSetVariablesPayload() throws JsonProcessingException {

        SetVariablesRequest payload = createSetVariablesRequest()
                .withComponent(DEFAULT_COMPONENT_NAME)
                .withVariable(DEFAULT_VARIABLE_NAME)
                .build();

        String callJson = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(SET_VARIABLES_ACTION)
                .withPayload(payload)
                .toJson();

        serverMessageHandler.handle(callJson);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SetVariablesRequest> requestCaptor = ArgumentCaptor.forClass(SetVariablesRequest.class);

        verify(setVariablesRequestHandlerMock).handle(messageIdCaptor.capture(), requestCaptor.capture());

        assertAll(
                () -> assertThat(messageIdCaptor.getValue()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(requestCaptor.getValue()).isInstanceOf(SetVariablesRequest.class)
        );

        CiString.CiString50 actualComponentName = requestCaptor.getValue().getSetVariableData().get(0).getComponent().getName();
        CiString.CiString50 actualVariableName = requestCaptor.getValue().getSetVariableData().get(0).getVariable().getName();

        assertAll(
                () -> assertThat(actualComponentName.toString()).isEqualTo(DEFAULT_COMPONENT_NAME),
                () -> assertThat(actualVariableName.toString()).isEqualTo(DEFAULT_VARIABLE_NAME)
        );

    }

    @Test
    void verifyCallMessageWithResetPayload() throws JsonProcessingException {

        ResetRequest payload = createResetRequest().withType(ResetRequest.Type.IMMEDIATE).build();

        String callJson = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(RESET_ACTION)
                .withPayload(payload)
                .toJson();

        serverMessageHandler.handle(callJson);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ResetRequest> requestCaptor = ArgumentCaptor.forClass(ResetRequest.class);

        verify(resetRequestHandlerMock).handle(messageIdCaptor.capture(), requestCaptor.capture());

        assertAll(
                () -> assertThat(messageIdCaptor.getValue()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(requestCaptor.getValue()).isInstanceOf(ResetRequest.class),
                () -> assertThat(requestCaptor.getValue().getType()).isEqualTo(ResetRequest.Type.IMMEDIATE)
        );

    }

    @Test
    void verifyCallMessageWithChangeAvailabilityPayload() throws JsonProcessingException {

        ChangeAvailabilityRequest payload = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        String callJson = createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(CHANGE_AVAILABILITY_ACTION)
                .withPayload(payload)
                .toJson();

        serverMessageHandler.handle(callJson);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChangeAvailabilityRequest> requestCaptor = ArgumentCaptor.forClass(ChangeAvailabilityRequest.class);

        verify(changeAvailabilityRequestHandlerMock).handle(messageIdCaptor.capture(), requestCaptor.capture());

        assertAll(
                () -> assertThat(messageIdCaptor.getValue()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(requestCaptor.getValue()).isInstanceOf(ChangeAvailabilityRequest.class)
        );

    }

    @Test
    void verifyCallResultMessage() throws JsonProcessingException {

        Object emptyPayload = new Object();

        Call call = new Call(DEFAULT_MESSAGE_ID, ActionType.BOOT_NOTIFICATION, emptyPayload);

        when(stationMessageSenderMock.getSentCalls()).thenReturn(ImmutableMap.of(DEFAULT_MESSAGE_ID, call));

        String callResultJson = createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withCurrentTime(ZonedDateTime.now().toString())
                .withIntervalInSeconds(1)
                .withStatus("Accepted")
                .toJson();

        serverMessageHandler.handle(callResultJson);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> callResultCaptor = ArgumentCaptor.forClass(Object.class);

        verify(subscriptionRegistryMock).fulfillSubscription(messageIdCaptor.capture(), callResultCaptor.capture());

        assertAll(
                () -> assertThat(messageIdCaptor.getValue()).isEqualTo(DEFAULT_MESSAGE_ID),
                () -> assertThat(callResultCaptor.getValue()).isInstanceOf(BootNotificationResponse.class)
        );
    }

    @Test
    void verifyCallErrorMessage() {

        Object emptyPayload = new Object();

        Call call = new Call(DEFAULT_MESSAGE_ID, ActionType.BOOT_NOTIFICATION, emptyPayload);

        when(stationMessageSenderMock.getSentCalls()).thenReturn(ImmutableMap.of(DEFAULT_MESSAGE_ID, call));

        String callErrorJson = createCallError()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withErrorCode("InternalError")
                .withErrorDescription("some error descr")
                .toJson();

        serverMessageHandler.handle(callErrorJson);


        verify(subscriptionRegistryMock, never()).fulfillSubscription(anyString(), any());

    }

    @Test
    void verifyCallMessageIsNotPresent() {

        String callErrorJson = createCallError()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withErrorCode("InternalError")
                .withErrorDescription("some error descr")
                .toJson();

        assertThrows(BadServerResponseException.class, () -> serverMessageHandler.handle(callErrorJson));

    }


    @Test
    void verifyInvalidAction() throws JsonProcessingException {

        String callJson = createCall()
                .withAction("Invalid action")
                .toJson();

        assertThrows(IllegalArgumentException.class, () -> serverMessageHandler.handle(callJson));

    }
}
