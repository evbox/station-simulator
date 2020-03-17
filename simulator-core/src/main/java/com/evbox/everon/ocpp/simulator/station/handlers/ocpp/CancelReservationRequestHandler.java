package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.common.Evse;
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
        CancelReservationResponse cancelReservationResponse = reservationOpt.map(reservation -> new CancelReservationResponse().withStatus(CancelReservationResponse.Status.ACCEPTED))
                .orElseGet(() -> new CancelReservationResponse().withStatus(CancelReservationResponse.Status.REJECTED));

        reservationOpt.ifPresent(reservation -> {
            if(isConnectorReserved(reservation)) {
                makeConnectorAvailable(reservation);
            }
            stationStore.removeReservation(reservation);
         });
        stationMessageSender.sendCallResult(callId, cancelReservationResponse);
    }

    private boolean isConnectorReserved(Reservation reservation) {
        return reservation.getEvse().getConnectorId() != null && stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId())
                    .map(Connector::getConnectorStatus)
                    .map(status -> StatusNotificationRequest.ConnectorStatus.RESERVED.equals(status))
                    .orElse(false);
    }

    private void makeConnectorAvailable(Reservation reservation) {
        stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId())
            .ifPresent(connector -> connector.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE));

        stationMessageSender.sendStatusNotification(reservation.getEvse().getId().intValue(), reservation.getEvse().getConnectorId().intValue(), StatusNotificationRequest.ConnectorStatus.AVAILABLE);
    }
}
