package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.factory.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.AVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.IN_PROGRESS;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangeEvseAvailabilityRequestTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @InjectMocks
    AvailabilityManager availabilityManager;

    @Captor
    ArgumentCaptor<ChangeAvailabilityResponse> changeAvailabilityResponseCaptor;

    @Test
    @DisplayName("Response should be send with ACCEPT status when requested EVSE status is the same as the current one")
    void shouldSendAcceptStatus() {
        // given
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        // when
        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        availabilityManager.changeEvseAvailability(DEFAULT_MESSAGE_ID, request, AVAILABLE);

        // then
        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.ACCEPTED);

    }

    @Test
    @DisplayName("Evse expectResponseFromStation connector should change status to UNAVAILABLE")
    void shouldChangeEvseAndConnectorStatus() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(INOPERATIVE);

        availabilityManager.changeEvseAvailability(DEFAULT_MESSAGE_ID, request, UNAVAILABLE);

        assertAll(
                () -> assertThat(evse.getEvseStatus()).isEqualTo(UNAVAILABLE),
                () -> assertThat(evse.getConnectors().get(0).getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.UNAVAILABLE)
        );

    }

    @Test
    @DisplayName("Send response with ACCEPT status expectResponseFromStation StatusNotification request for every connector")
    void shouldSendAcceptStatusAndStatusNotification() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(UNAVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        availabilityManager.changeEvseAvailability(DEFAULT_MESSAGE_ID, request, AVAILABLE);

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
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(new EvseTransaction(DEFAULT_TRANSACTION_ID, IN_PROGRESS))
                .build();

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenReturn(evse);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(INOPERATIVE);

        availabilityManager.changeEvseAvailability(DEFAULT_MESSAGE_ID, request, UNAVAILABLE);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertAll(
                () -> assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.SCHEDULED),
                () -> assertThat(evse.getScheduledNewEvseStatus()).isEqualTo(UNAVAILABLE)
        );

    }

    @Test
    @DisplayName("Throw exception on invalid evseId")
    void shouldSendRejectedOnInvalidEvseId() {

        when(stationStateMock.findEvse(eq(DEFAULT_EVSE_ID))).thenThrow(new IllegalArgumentException());

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        availabilityManager.changeEvseAvailability(DEFAULT_MESSAGE_ID, request, UNAVAILABLE);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.REJECTED);

    }

}
