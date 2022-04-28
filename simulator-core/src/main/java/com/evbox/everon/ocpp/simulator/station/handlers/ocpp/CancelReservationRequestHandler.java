package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.model.Reservation;
import com.evbox.everon.ocpp.v201.message.common.Evse;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationRequest;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationResponse;
import com.evbox.everon.ocpp.v201.message.station.CancelReservationStatus;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public class CancelReservationRequestHandler implements OcppRequestHandler<CancelReservationRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationStore stationStore;

    @Override
    public void handle(String callId, CancelReservationRequest request) {
        Optional<Reservation> reservationOpt = stationStore.tryFindReservationById(request.getReservationId());
        reservationOpt.ifPresentOrElse(
                r -> cancelReservation(callId, r),
                ()-> respondWithReject(callId));
    }

    private void cancelReservation(String callId, Reservation reservation) {
        ofNullable(reservation.getEvse())
                .map(Evse::getId)
                .flatMap(stationStore::tryFindEvse)
                .ifPresent(evse -> evse.getConnectorsInStatus(ConnectorStatus.RESERVED)
                        .forEach(connector -> {
                            evse.setConnectorAvailable(connector.getId());
                            stationMessageSender.sendStatusNotification(evse.getId(), connector.getId(), ConnectorStatus.AVAILABLE);
                        }));

        stationStore.removeReservation(reservation);
        stationMessageSender.sendCallResult(callId, new CancelReservationResponse().withStatus(CancelReservationStatus.ACCEPTED));
    }

    private void respondWithReject(String callId) {
        stationMessageSender.sendCallResult(callId, new CancelReservationResponse().withStatus(CancelReservationStatus.REJECTED));
    }
}
