package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseState;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse.Status.ACCEPTED;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse.Status.REJECTED;

/**
 * Handler for {@link ChangeAvailabilityRequest} request.
 */
@Slf4j
@AllArgsConstructor
public class ChangeAvailabilityRequestHandler implements OcppRequestHandler<ChangeAvailabilityRequest> {

    private final StationState stationState;
    private final StationMessageSender stationMessageSender;

    /**
     * Handle {@link ChangeAvailabilityRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ChangeAvailabilityRequest request) {
        try {
            Evse evse = stationState.findEvse(request.getEvseId());

            EvseState requestedEvseState = AvailabilityStateMapper.mapFrom(request.getOperationalStatus());

            if (evse.hasRequestedState(requestedEvseState)) {

                sendResponseWithStatus(callId, ACCEPTED);

            } else {

                sendResponseWithStatus(callId, ACCEPTED);

                evse.setEvseState(requestedEvseState);

                // for every connector send StatusNotification request
                for (Connector connector : evse.getConnectors()) {
                    stationMessageSender.sendStatusNotification(evse, connector);
                }

            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sendResponseWithStatus(callId, REJECTED);
        }

    }

    private void sendResponseWithStatus(String callId, ChangeAvailabilityResponse.Status status) {
        stationMessageSender.sendCallResult(callId, new ChangeAvailabilityResponse().withStatus(status));
    }

}
