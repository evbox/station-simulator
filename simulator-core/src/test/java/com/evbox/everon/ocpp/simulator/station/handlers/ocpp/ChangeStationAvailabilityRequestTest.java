package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
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

import java.util.Arrays;
import java.util.Collections;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.support.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangeStationAvailabilityRequestTest {


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

        when(availabilityStateMapperMock.mapFrom(eq(OPERATIVE))).thenReturn(EvseStatus.AVAILABLE);

        // when
        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(EVSE_ID_ZERO).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        // then
        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.ACCEPTED);

    }

    @Test
    @DisplayName("Evse and connector should change status to UNAVAILABLE")
    void shouldChangeEvseAndConnectorStatus() {
        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withStatus(EvseStatus.AVAILABLE)
                .withConnectorId(DEFAULT_CONNECTOR_ID)
                .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
                .withCableStatus(CableStatus.UNPLUGGED)
                .withTransaction(EvseTransaction.NONE)
                .build();

        when(stationStateMock.getEvses()).thenReturn(Collections.singletonList(evse));

        when(availabilityStateMapperMock.mapFrom(eq(INOPERATIVE))).thenReturn(UNAVAILABLE);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(EVSE_ID_ZERO).withOperationalStatus(INOPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        assertAll(
                () -> assertThat(evse.getEvseStatus()).isEqualTo(UNAVAILABLE),
                () -> assertThat(evse.getConnectors().get(0).getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.UNAVAILABLE)
        );

    }

    @Test
    @DisplayName("Response should be send with SCHEDULED status when requested EVSE has transaction in progress")
    void shouldSendScheduledStatus() {
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

        when(availabilityStateMapperMock.mapFrom(any(ChangeAvailabilityRequest.OperationalStatus.class))).thenReturn(UNAVAILABLE);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(EVSE_ID_ZERO).withOperationalStatus(INOPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(eq(DEFAULT_MESSAGE_ID), changeAvailabilityResponseCaptor.capture());

        ChangeAvailabilityResponse response = changeAvailabilityResponseCaptor.getValue();

        assertAll(
                () -> assertThat(response.getStatus()).isEqualTo(ChangeAvailabilityResponse.Status.SCHEDULED)
        );

    }

}
