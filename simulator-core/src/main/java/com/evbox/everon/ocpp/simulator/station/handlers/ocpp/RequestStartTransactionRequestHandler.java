package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.v201.message.station.RequestStartStopStatus;
import com.evbox.everon.ocpp.v201.message.station.RequestStartTransactionRequest;
import com.evbox.everon.ocpp.v201.message.station.RequestStartTransactionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Handler for {@link RequestStartTransactionRequest} request.
 */
@Slf4j
public class RequestStartTransactionRequestHandler implements OcppRequestHandler<RequestStartTransactionRequest> {

    private static final Set<String> ALLOWED_STATES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(AvailableState.NAME, WaitingForAuthorizationState.NAME)));

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
            String evseState = stateManager.getStateForEvse(evse.getId()).getStateName();

            if (!ALLOWED_STATES.contains(evseState)) {
                log.debug("Evse not available");
                stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartStopStatus.REJECTED));
                return;
            }

            Connector connector = evse.tryFindAvailableConnector().orElse(null);
            RequestStartTransactionResponse response = new RequestStartTransactionResponse().withStatus(RequestStartStopStatus.ACCEPTED);

            if (WaitingForAuthorizationState.NAME.equals(evseState)) {
                // Allowed to remote start only if there is an ongoing transaction
                if (!evse.hasOngoingTransaction()) {
                    log.debug("Evse has no ongoing transaction");
                    stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartStopStatus.REJECTED));
                    return;
                }

                connector = evse.tryFindPluggedConnector().orElse(null);
                response = response.withTransactionId(new CiString.CiString36(evse.getTransaction().getTransactionId()));
            }

            if (connector == null) {
                log.debug("Connector not found");
                stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartStopStatus.REJECTED));
                return;
            }

            stationMessageSender.sendCallResult(callId, response);

            stateManager.remoteStart(evse.getId(), request.getRemoteStartId(), request.getIdToken().getIdToken().toString(), connector);
        } else {
            log.debug("No available evse to start a new transaction");
            stationMessageSender.sendCallResult(callId, new RequestStartTransactionResponse().withStatus(RequestStartStopStatus.REJECTED));
        }
    }

}
