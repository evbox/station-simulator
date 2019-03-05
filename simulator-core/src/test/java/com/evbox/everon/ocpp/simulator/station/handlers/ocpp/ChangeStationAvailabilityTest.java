package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
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

import java.util.Arrays;
import java.util.Collections;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.AVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.support.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangeStationAvailabilityTest {


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

        when(stationStateMock.getEvses()).thenReturn(Collections.singletonList(evse));

        // when
        availabilityManager.changeStationAvailability(DEFAULT_MESSAGE_ID, EvseStatus.AVAILABLE);

        // then
        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.ACCEPTED);

    }

    @Test
    @DisplayName("Evse and connector should change status to UNAVAILABLE")
    void shouldChangeEvseAndConnectorStatus() {
        // given
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.getEvses()).thenReturn(Collections.singletonList(evse));

        // when
        availabilityManager.changeStationAvailability(DEFAULT_MESSAGE_ID, UNAVAILABLE);

        // then
        assertAll(
                () -> assertThat(evse.getEvseStatus()).isEqualTo(UNAVAILABLE),
                () -> assertThat(evse.getConnectors().get(0).getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.UNAVAILABLE)
        );

    }

    @Test
    @DisplayName("Response should be send with SCHEDULED status when requested EVSE has transaction in progress")
    void shouldSendScheduledStatus() {
        // given
        Evse evse1 = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(new EvseTransaction(DEFAULT_INT_TRANSACTION_ID, IN_PROGRESS))
                .build();
        Evse evse2 = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.getEvses()).thenReturn(Arrays.asList(evse1, evse2));

        // when
        availabilityManager.changeStationAvailability(DEFAULT_MESSAGE_ID, UNAVAILABLE);

        // then
        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertAll(
                () -> assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.SCHEDULED)
        );

    }

    @Test
    @DisplayName("Should set scheduled status for further processing")
    void shouldSetScheduledStatus() {
        // given
        Evse evse1 = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(new EvseTransaction(DEFAULT_INT_TRANSACTION_ID, IN_PROGRESS))
                .build();
        Evse evse2 = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.getEvses()).thenReturn(Arrays.asList(evse1, evse2));

        // when
        availabilityManager.changeStationAvailability(DEFAULT_MESSAGE_ID, UNAVAILABLE);

        assertAll(
                () -> assertThat(evse1.getEvseStatus()).isEqualTo(AVAILABLE),
                () -> assertThat(evse1.getScheduledNewEvseStatus()).isEqualTo(UNAVAILABLE),
                () -> assertThat(evse2.getEvseStatus()).isEqualTo(UNAVAILABLE),
                () -> assertThat(evse2.getScheduledNewEvseStatus()).isNull()
        );

    }

    @Test
    @DisplayName("Response should be send with REJECTED status when no EVSEs are present")
    void shouldSendRejectedStatus() {
        // given
        when(stationStateMock.getEvses()).thenReturn(Collections.EMPTY_LIST);

        // when
        availabilityManager.changeStationAvailability(DEFAULT_MESSAGE_ID, UNAVAILABLE);

        // then
        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertAll(
                () -> assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.REJECTED)
        );

    }



}
