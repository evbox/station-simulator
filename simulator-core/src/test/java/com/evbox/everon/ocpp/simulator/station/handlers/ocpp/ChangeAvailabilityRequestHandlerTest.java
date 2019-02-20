package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChangeAvailabilityRequestHandlerTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;

    ChangeAvailabilityRequestHandler changeAvailabilityRequestHandler;

    @Captor
    ArgumentCaptor<ChangeAvailabilityResponse> changeAvailabilityResponseCaptor;

    @BeforeEach
    void setUp() {
        changeAvailabilityRequestHandler = new ChangeAvailabilityRequestHandler(stationStateMock, stationMessageSenderMock);
    }

    @Test
    @DisplayName("Response should be send with ACCEPT status when requested EVSE state is the same as the current one")
    void shouldSendAcceptStatus() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.AVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(new EvseTransaction(EvseTransactionState.NONE))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.ACCEPTED);

    }

    @Test
    @DisplayName("Send response with ACCEPT status and StatusNotification request for every connector")
    void shouldSendAcceptStatusAndStatusNotification() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.UNAVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(new EvseTransaction(EvseTransactionState.NONE))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.ACCEPTED);

        Connector connector = evse.getConnectors().get(0);

        verify(stationMessageSenderMock).sendStatusNotification(eq(evse), eq(connector));

    }


    @Test
    @DisplayName("Response should be send with SCHEDULED status when requested EVSE has transaction in progress")
    void shouldSendScheduledStatus() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.AVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(new EvseTransaction(EvseTransactionState.IN_PROGRESS))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.SCHEDULED);

    }

    @Test
    @DisplayName("Response should be send with REJECTED status when exception occurs")
    void shouldSendRejectStatus() {

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenThrow(new IllegalStateException("some runtime exception"));

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        assertThrows(IllegalStateException.class, () -> changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request));

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.REJECTED);

    }

    @Test
    @DisplayName("Should throw an exception if sending can not be done")
    void shouldThrowException() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.AVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(new EvseTransaction(EvseTransactionState.NONE))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        doThrow(new IllegalStateException("some runtime exception"))
                .when(stationMessageSenderMock).sendCallResult(anyString(), any(ChangeAvailabilityResponse.class));

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        assertThrows(IllegalStateException.class, () -> changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request));

        verify(stationMessageSenderMock, times(2)).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

    }
}
