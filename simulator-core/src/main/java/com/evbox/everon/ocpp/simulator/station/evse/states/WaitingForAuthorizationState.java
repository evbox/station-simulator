package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.states.helpers.AuthorizeHelper;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v201.message.station.AuthorizationStatus;
import com.evbox.everon.ocpp.v201.message.station.AuthorizeResponse;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.*;
import static java.util.Collections.singletonList;

/**
 * When the cable has been plugged but the user is not authorized yet.
 */
@Slf4j
public class WaitingForAuthorizationState extends AbstractEvseState {

    public static final String NAME = "WAITING_FOR_AUTHORIZATION";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public CompletableFuture<UserMessageResult> onPlug(int evseId, int connectorId) {
        return CompletableFuture.completedFuture(UserMessageResult.NOT_EXECUTED);
    }

    @Override
    public CompletableFuture<UserMessageResult> onAuthorize(int evseId, String tokenId) {
        log.info("in authorizeToken {}", tokenId);

        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId),
                (request, response) -> handleAuthorizeResponse(evseId, tokenId, response, future));

        return future;
    }

    private void handleAuthorizeResponse(int evseId, String tokenId, AuthorizeResponse response, CompletableFuture<UserMessageResult> future) {
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        StationStore stationStore = stateManager.getStationStore();
        Evse evse = stationStore.findEvse(evseId);
        if (response.getIdTokenInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            evse.setToken(tokenId);

            if (!evse.hasOngoingTransaction()) {
                String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId);
            }

            int connectorId = evse.getPluggedConnector();
            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, tokenId, com.evbox.everon.ocpp.v201.message.station.ChargingState.EV_CONNECTED);

            connectorId = startCharging(evse);
            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, CHARGING_STATE_CHANGED, tokenId, com.evbox.everon.ocpp.v201.message.station.ChargingState.CHARGING);

            stateManager.setStateForEvse(evseId, new ChargingState());
            future.complete(UserMessageResult.SUCCESSFUL);
        } else {
            AuthorizeHelper.handleFailedAuthorizeResponse(stateManager, evse);
            future.complete(UserMessageResult.FAILED);
        }
    }

    @Override
    public CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId) {
        Evse evse = stateManager.getStationStore().findEvse(evseId);

        if (evse.findConnector(connectorId).getCableStatus() == CableStatus.LOCKED) {
            throw new IllegalStateException(String.format("Unable to unplug locked connector: %d %d", evseId, connectorId));
        }

        evse.unplug(connectorId);
        evse.clearToken();

        if (evse.hasOngoingTransaction()) {
            evse.stopTransaction();
            StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

            stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (request, response) ->
                    stationMessageSender.sendTransactionEventEnded(evse.getId(), connectorId,
                                                                    TriggerReason.EV_DEPARTED,
                                                                    Reason.EV_DISCONNECTED, evse.getWattConsumedLastSession()));
        }

        stateManager.setStateForEvse(evseId, new AvailableState());
        return CompletableFuture.completedFuture(UserMessageResult.SUCCESSFUL);
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);
        evse.setToken(tokenId);

        startCharging(evse);
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connector.getId(), REMOTE_START, tokenId, com.evbox.everon.ocpp.v201.message.station.ChargingState.CHARGING);
        stateManager.setStateForEvse(evseId, new ChargingState());
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    private int startCharging(Evse evse) {
        Integer connectorId = evse.lockPluggedConnector();
        evse.startCharging();
        return connectorId;
    }
}
