package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.v20.message.station.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Handler for {@link RequestStartTransactionRequest} request.
 */
@Slf4j
public class RequestStartTransactionRequestHandler implements OcppRequestHandler<RequestStartTransactionRequest> {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;
    private final StateManager stateManager;

    public RequestStartTransactionRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender, StateManager stateManager) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
        this.stateManager = stateManager;
    }

    /**
     * Handle {@link RequestStartTransactionRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, RequestStartTransactionRequest request) {

        Optional<Evse> optionalEvse = Optional.ofNullable(request.getEvseId())
                                        .map(stationStore::tryFindEvse)
                                        .orElseGet(stationStore::tryFindAvailableEvse);

        if (optionalEvse.isPresent()) {
            Evse evse = optionalEvse.get();
            Connector connector = evse.tryFindAvailableConnector().orElse(null);

            if (!AvailableState.NAME.equals(stateManager.getStateForEvse(evse.getId()).getStateName())) {
                log.debug("Evse not available");
                stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.REJECTED));
                return;
            }

            if (connector == null || connector.getCableStatus() != CableStatus.UNPLUGGED) {
                log.debug("Connector not available");
                stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.REJECTED));
                return;
            }
            stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.ACCEPTED));

            stateManager.remoteStart(evse.getId(), request.getRemoteStartId(), request.getIdToken().getIdToken().toString(), connector);
        } else {
            log.debug("No available evse to start a new transaction");
            stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartTransactionResponse.Status.REJECTED));
        }
    }

}
