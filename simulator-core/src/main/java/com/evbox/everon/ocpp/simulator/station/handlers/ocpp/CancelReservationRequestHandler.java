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
            if (reservation.getEvse().getConnectorId() != null && isConnectorReserved(reservation)) {
                makeConnectorAvailable(reservation);
                stationMessageSender.sendStatusNotification(reservation.getEvse().getId().intValue(), reservation.getEvse().getConnectorId().intValue(), StatusNotificationRequest.ConnectorStatus.AVAILABLE);
            }
            stationStore.removeReservation(reservation);
        });
        stationMessageSender.sendCallResult(callId, cancelReservationResponse);
    }

    private boolean isConnectorReserved(Reservation reservation) {
        return stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId())
                    .map(connector -> StatusNotificationRequest.ConnectorStatus.RESERVED.equals(connector.getConnectorStatus()))
                    .orElse(false);
    }

    private void makeConnectorAvailable(Reservation reservation) {
        stationStore.tryFindConnector(reservation.getEvse().getId(), reservation.getEvse().getConnectorId())
            .ifPresent(theConnector -> theConnector.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE));
    }
}
