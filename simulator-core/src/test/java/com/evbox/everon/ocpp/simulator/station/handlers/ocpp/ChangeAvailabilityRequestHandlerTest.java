package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseState.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionState.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.support.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangeAvailabilityRequestHandlerTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    AvailabilityStateMapper availabilityStateMapperMock;
    @InjectMocks
    ChangeAvailabilityRequestHandler changeAvailabilityRequestHandler;

    @Captor
    ArgumentCaptor<ChangeAvailabilityResponse> changeAvailabilityResponseCaptor;

    @Test
    @DisplayName("Response should be send with ACCEPT status when requested EVSE state is the same as the current one")
    void shouldSendAcceptStatus() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.AVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
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
                .withState(UNAVAILABLE)
                .withConnectorIdAndState(DEFAULT_CONNECTOR_ID, ConnectorState.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
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
                .withTransaction(new EvseTransaction(DEFAULT_INT_TRANSACTION_ID, IN_PROGRESS))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);
        when(availabilityStateMapperMock.mapFrom(any(OperationalStatus.class))).thenReturn(UNAVAILABLE);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(INOPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertAll(
                () -> assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.SCHEDULED),
                () -> assertThat(evse.getScheduleNewEvseState()).isEqualTo(UNAVAILABLE)
        );

    }

    @Test
    @DisplayName("Throw exception on invalid evseId")
    void shouldThrowExceptionOnInvalidEvseId() {

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenThrow(new IllegalArgumentException("some runtime exception"));

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        assertThrows(IllegalArgumentException.class, () -> changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request));

    }

    @Test
    @DisplayName("Should throw an exception on invalid operational status")
    void shouldThrowExceptionOnInvalidOperationalStatus() {

        when(availabilityStateMapperMock.mapFrom(any(OperationalStatus.class))).thenThrow(new IllegalArgumentException("some runtime exception"));

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        assertThrows(IllegalArgumentException.class, () -> changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request));

    }
}
