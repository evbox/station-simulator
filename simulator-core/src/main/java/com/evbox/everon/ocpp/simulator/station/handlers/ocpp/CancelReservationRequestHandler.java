package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.station.CancelReservationRequest;
import com.evbox.everon.ocpp.v20.message.station.CancelReservationResponse;
import com.evbox.everon.ocpp.v20.message.station.Reservation;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CancelReservationRequestHandler implements OcppRequestHandler<CancelReservationRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationStore stationStore;

    @Override
    public void handle(String callId, CancelReservationRequest request) {
        Optional<Reservation> reservationOpt = stationStore.tryFindReservationById(request.getReservationId());

        if (!reservationOpt.isPresent()) {
            stationMessageSender.sendCallResult(callId, new CancelReservationResponse().withStatus(CancelReservationResponse.Status.REJECTED));
        } else {
            Reservation reservation = reservationOpt.get();
            stationMessageSender.sendCallResult(callId, new CancelReservationResponse().withStatus(CancelReservationResponse.Status.ACCEPTED));

            if (isConnectorReserved(reservation)) {
                makeConnectorAvailable(reservation);
                stationMessageSender.sendStatusNotification(reservation.getEvse().getId().intValue(), reservation.getEvse().getConnectorId().intValue(), StatusNotificationRequest.ConnectorStatus.AVAILABLE);
            }

            stationStore.removeReservation(reservation);
        }
    }

    private boolean isConnectorReserved(Reservation reservation) {
        if (reservation.getEvse().getConnectorId() != null)  {
            Optional<Connector> connector = stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId());
            if (connector.isPresent()) {
                return StatusNotificationRequest.ConnectorStatus.RESERVED.equals(connector.get().getConnectorStatus());
            }
        }
        return false;
    }

    private void makeConnectorAvailable(Reservation reservation) {
        Optional<Connector> connector = stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId());

        if (connector.isPresent()) {
            connector.get().setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        }
    }
}
