package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse.Status.*;

/**
 * Handler for {@link ChangeAvailabilityRequest} request.
 */
@Slf4j
public class ChangeAvailabilityRequestHandler implements OcppRequestHandler<ChangeAvailabilityRequest> {

    private final StationState stationState;
    private final StationMessageSender stationMessageSender;
    private final AvailabilityStateMapper availabilityStateMapper;

    /**
     * Create an instance.
     *
     * @param stationState         {@link StationState}
     * @param stationMessageSender {@link StationMessageSender}
     */
    public ChangeAvailabilityRequestHandler(StationState stationState, StationMessageSender stationMessageSender) {
        this(stationState, stationMessageSender, new AvailabilityStateMapper());
    }

    /**
     * Create an instance.
     *
     * @param stationState         {@link StationState}
     * @param stationMessageSender {@link StationMessageSender}
     */
    public ChangeAvailabilityRequestHandler(StationState stationState, StationMessageSender stationMessageSender, AvailabilityStateMapper availabilityStateMapper) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
        this.availabilityStateMapper = availabilityStateMapper;
    }

    /**
     * Handle {@link ChangeAvailabilityRequest} request.
     * <p>
     * It has 2 scenarios:
     * <p>
     * -- when evseId == 0 then:
     * Iterate over all evses that station has and do the following:
     * <p>
     * 1. Send response with ACCEPTED status when EVSE status is the same as requested.
     * 2. Change EVSE status to the requested status when they do not match.
     * In addition send response with ACCEPTED status and StatusNotification request for every EVSE Connector.
     * 3. When a transaction is in progress.
     * Send response with SCHEDULED status and save scheduled status for further processing.
     * <p>
     * -- when evseId != 0 then do the same as written above but only for one evse
     *
     * @param callId  identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ChangeAvailabilityRequest request) {

        EvseStatus requestedEvseStatus = availabilityStateMapper.mapFrom(request.getOperationalStatus());

        ChangeAvailabilityResponse.Status statusToSend = null;

        if (changeEvseAvailability(request)) {

            statusToSend = stationState.tryFindEvse(request.getEvseId())
                    .map(evse -> handleEvseStatus(requestedEvseStatus, evse))
                    .orElse(REJECTED);

        } else {

            for (Evse evse : stationState.getEvses()) {

                ChangeAvailabilityResponse.Status evseStatus = handleEvseStatus(requestedEvseStatus, evse);

                if (changeStatusIsNeeded(statusToSend)) {
                    statusToSend = evseStatus;
                }
            }

        }

        sendResponseWithStatus(callId, statusToSend);

    }

    private ChangeAvailabilityResponse.Status handleEvseStatus(EvseStatus requestedEvseStatus, Evse evse) {

        if (!evse.hasStatus(requestedEvseStatus)) {

            if (evse.hasOngoingTransaction()) {

                evse.setScheduledNewEvseStatus(requestedEvseStatus);

                sendNotificationRequest(evse);

                return SCHEDULED;

            }

            evse.changeStatus(requestedEvseStatus);
            sendNotificationRequest(evse);

        }

        return ACCEPTED;

    }

    private void sendNotificationRequest(Evse evse) {

        // for every connector send StatusNotification request
        for (Connector connector : evse.getConnectors()) {
            stationMessageSender.sendStatusNotification(evse, connector);
        }

    }

    private boolean changeStatusIsNeeded(ChangeAvailabilityResponse.Status statusToSend) {
        return statusToSend != SCHEDULED;
    }

    private boolean changeEvseAvailability(ChangeAvailabilityRequest request) {
        return request.getEvseId() != 0;
    }

    private void sendResponseWithStatus(String callId, ChangeAvailabilityResponse.Status status) {
        stationMessageSender.sendCallResult(callId, new ChangeAvailabilityResponse().withStatus(status));
    }

}
