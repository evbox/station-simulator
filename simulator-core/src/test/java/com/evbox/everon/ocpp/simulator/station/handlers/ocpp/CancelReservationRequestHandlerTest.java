package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.common.Evse;
import com.evbox.everon.ocpp.v20.message.station.CancelReservationRequest;
import com.evbox.everon.ocpp.v20.message.station.CancelReservationResponse;
import com.evbox.everon.ocpp.v20.message.station.Reservation;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelReservationRequestHandlerTest {

    private static final String CALL_ID = "123";
    private static final int RESERVATION_ID = 123;
    private static final int CONNECTOR_ID = 1;
    private static final int EVSE_ID = 2;

    @Mock
    private StationMessageSender stationMessageSender;

    @Mock
    private StationStore stationStore;

    @InjectMocks
    private CancelReservationRequestHandler handler;

    @Captor
    private ArgumentCaptor<CancelReservationResponse> cancelReservationResponseArgumentCaptor = ArgumentCaptor.forClass(CancelReservationResponse.class);

    @Test
    public void testCancelReservationRejected() {
        assertCancelReservationStatus(CancelReservationResponse.Status.REJECTED);
    }

    @Test
    public void testCancelReservationAcceptedWithNoConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID, null);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));

        assertCancelReservationStatus(CancelReservationResponse.Status.ACCEPTED);
    }

    @Test
    public void testCancelReservationAcceptedWithConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID, CONNECTOR_ID);
        Connector connector = new Connector(CONNECTOR_ID, CableStatus.PLUGGED, StatusNotificationRequest.ConnectorStatus.RESERVED);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));
        when(stationStore.tryFindConnector(anyInt(), anyInt())).thenReturn(Optional.of(connector));

        assertCancelReservationStatus(CancelReservationResponse.Status.ACCEPTED);
        verify(stationMessageSender).sendStatusNotification(EVSE_ID, CONNECTOR_ID, StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        assertThat(connector.getConnectorStatus()).isEqualTo(StatusNotificationRequest.ConnectorStatus.AVAILABLE);
    }

    private void assertCancelReservationStatus(CancelReservationResponse.Status status) {
        handler.handle(CALL_ID, new CancelReservationRequest().withReservationId(RESERVATION_ID));

        verify(stationMessageSender).sendCallResult(anyString(), cancelReservationResponseArgumentCaptor.capture());

        CancelReservationResponse response = cancelReservationResponseArgumentCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getAdditionalProperties()).isEmpty();
    }

    private Reservation buildReservation(Integer evseId, Integer reservationId, Integer connectorId) {
        Evse evse = new Evse().withId(evseId).withConnectorId(connectorId);
        return new Reservation().withId(reservationId).withEvse(evse);
    }
}
