package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseState;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.ChangeAvailabilityFuture;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityResponse.Status.*;

/**
 * Handler for {@link ChangeAvailabilityRequest} request.
 */
@Slf4j
public class ChangeAvailabilityRequestHandler implements OcppRequestHandler<ChangeAvailabilityRequest> {

    private static final int DEFAULT_NUMBER_OF_ATTEMPTS = 30;
    private static final int DELAY_BETWEEN_ATTEMPTS = 1_000;

    private final StationState stationState;
    private final StationMessageSender stationMessageSender;
    private final int attemptsInMillis;
    private final int delayBetweenAttemptsInMillis;

    private final ChangeAvailabilityFuture changeAvailabilityFuture = new ChangeAvailabilityFuture();

    /**
     * Create an instance.
     *
     * @param stationState         {@link StationState}
     * @param stationMessageSender {@link StationMessageSender}
     */
    public ChangeAvailabilityRequestHandler(StationState stationState, StationMessageSender stationMessageSender) {
        this(stationState, stationMessageSender, DEFAULT_NUMBER_OF_ATTEMPTS, DELAY_BETWEEN_ATTEMPTS);
    }

    /**
     * Create an instance.
     *
     * @param stationState                 {@link StationState}
     * @param stationMessageSender         {@link StationMessageSender}
     * @param attemptsInMillis             number of attempts to try to change the state before giving up
     * @param delayBetweenAttemptsInMillis delay between attempts
     */
    public ChangeAvailabilityRequestHandler(StationState stationState, StationMessageSender stationMessageSender,
                                            int attemptsInMillis, int delayBetweenAttemptsInMillis) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
        this.attemptsInMillis = attemptsInMillis;
        this.delayBetweenAttemptsInMillis = delayBetweenAttemptsInMillis;
    }

    /**
     * Handle {@link ChangeAvailabilityRequest} request.
     * It has 4 scenarios:
     * 1. Send response with ACCEPTED status when EVSE state is the same as requested.
     * 2. Change EVSE state to the requested state when they do not match.
     * In addition send response with ACCEPTED status and StatusNotification request for every EVSE Connector.
     * 3. When a transaction is in progress.
     * Send response with SCHEDULED status and schedule a task to change the EVSE state when transaction is finished.
     * 4. Send response with REJECTED status if exception occurs.
     *
     * @param callId  identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ChangeAvailabilityRequest request) {
        try {
            Evse evse = stationState.findEvse(request.getEvseId());

            EvseState requestedEvseState = AvailabilityStateMapper.mapFrom(request.getOperationalStatus());

            if (evse.hasOngoingTransaction()) {

                changeAvailabilityFuture.runAsync(evse, requestedEvseState, attemptsInMillis, delayBetweenAttemptsInMillis);

                sendResponseWithStatus(callId, SCHEDULED);

            } else {

                sendResponseWithStatus(callId, ACCEPTED);

                if (!evse.hasState(requestedEvseState)) {

                    evse.setEvseState(requestedEvseState);

                    // for every connector send StatusNotification request
                    for (Connector connector : evse.getConnectors()) {
                        stationMessageSender.sendStatusNotification(evse, connector);
                    }

                }

            }

        } catch (Exception e) {
            sendResponseWithStatus(callId, REJECTED);
            throw e;
        }

    }

    private void sendResponseWithStatus(String callId, ChangeAvailabilityResponse.Status status) {
        stationMessageSender.sendCallResult(callId, new ChangeAvailabilityResponse().withStatus(status));
    }

}
