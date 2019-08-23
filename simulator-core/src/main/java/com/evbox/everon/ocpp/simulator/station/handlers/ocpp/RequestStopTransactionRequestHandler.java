package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionRequest;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionResponse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.extern.slf4j.Slf4j;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.REMOTE_STOP;

/**
 * Handler for {@link RequestStopTransactionRequest} request.
 */
@Slf4j
public class RequestStopTransactionRequestHandler implements OcppRequestHandler<RequestStopTransactionRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationState stationState;

    public RequestStopTransactionRequestHandler(StationMessageSender stationMessageSender, StationState stationState) {
        this.stationMessageSender = stationMessageSender;
        this.stationState = stationState;
    }

    /**
     * Handle {@link RequestStopTransactionRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, RequestStopTransactionRequest request) {

        String transactionId = request.getTransactionId().toString();
        try {
            Evse evse = stationState.findEvseByTransactionId(transactionId);

            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.ACCEPTED));

            stopCharging(evse);
        } catch (IllegalArgumentException e) {
            log.debug("Received RequestStopTransactionRequest with invalid transactionID: " + e.getMessage());
            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.REJECTED));
        }
    }

    private void stopCharging(Evse evse) {
        evse.remotelyStopCharging();
        Integer connectorId = evse.unlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, REMOTE_STOP, TransactionData.ChargingState.EV_DETECTED);
    }
}
