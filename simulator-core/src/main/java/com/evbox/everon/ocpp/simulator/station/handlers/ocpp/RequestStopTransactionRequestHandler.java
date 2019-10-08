package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionRequest;
import com.evbox.everon.ocpp.v20.message.station.RequestStopTransactionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Handler for {@link RequestStopTransactionRequest} request.
 */
@Slf4j
public class RequestStopTransactionRequestHandler implements OcppRequestHandler<RequestStopTransactionRequest> {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;
    private final StateManager stateManager;

    public RequestStopTransactionRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender, StateManager stateManager) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
        this.stateManager = stateManager;
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
        Optional<Evse> evse = stationStore.tryFindEvseByTransactionId(transactionId);
        if (evse.isPresent()) {
            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.ACCEPTED));
            stateManager.remoteStop(evse.get().getId());
        } else {
            log.debug("Received RequestStopTransactionRequest with invalid transactionID: " + transactionId);
            stationMessageSender.sendCallResult(callId, new RequestStopTransactionResponse().withStatus(RequestStopTransactionResponse.Status.REJECTED));
        }
    }
}
