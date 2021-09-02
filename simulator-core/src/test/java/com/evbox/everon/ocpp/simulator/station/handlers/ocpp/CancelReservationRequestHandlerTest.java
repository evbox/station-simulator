package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.model.Reservation;
import com.evbox.everon.ocpp.v201.message.common.Evse;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationRequest;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationResponse;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationStatus;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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
        assertCancelReservationStatus(CancelReservationStatus.REJECTED);
    }

    @Test
    public void testCancelReservationAcceptedWithNoConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));

        assertCancelReservationStatus(CancelReservationStatus.ACCEPTED);
    }

    @Test
    public void testCancelReservationAcceptedWithConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID);
        Connector connector = new Connector(CONNECTOR_ID, CableStatus.PLUGGED, ConnectorStatus.RESERVED);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));
        when(stationStore.tryFindConnectors(anyInt())).thenReturn(List.of(connector));

        assertCancelReservationStatus(CancelReservationStatus.ACCEPTED);
        verify(stationMessageSender).sendStatusNotification(EVSE_ID, CONNECTOR_ID, ConnectorStatus.AVAILABLE);
        assertThat(connector.getConnectorStatus()).isEqualTo(ConnectorStatus.AVAILABLE);
    }

    private void assertCancelReservationStatus(CancelReservationStatus status) {
        handler.handle(CALL_ID, new CancelReservationRequest().withReservationId(RESERVATION_ID));

        verify(stationMessageSender).sendCallResult(anyString(), cancelReservationResponseArgumentCaptor.capture());

        CancelReservationResponse response = cancelReservationResponseArgumentCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(status);
    }

    private Reservation buildReservation(Integer evseId, Integer reservationId) {
        Evse evse = new Evse().withId(evseId);
        return new Reservation(reservationId, evse);
    }
}
