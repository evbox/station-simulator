package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v201.message.station.AuthorizationStatus;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import static com.evbox.everon.ocpp.v201.message.station.ChargingState.CHARGING;
import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.AUTHORIZED;
import static java.util.Collections.singletonList;

/**
 * When the evse has been stopped from charging, remotely or locally
 */
@Slf4j
public class StoppedState extends AbstractEvseState {

    public static final String NAME = "STOPPED";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public CompletableFuture<UserMessageResult> onPlug(int evseId, int connectorId) {
        return CompletableFuture.completedFuture(UserMessageResult.NOT_EXECUTED);
    }

    @Override
    public CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);
        evse.unplug(connectorId);

        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (request, response) -> {
            OptionList<TxStartStopPointVariableValues> stopPoints = stationStore.getTxStopPointValues();
            if (!stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) || stopPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                evse.stopTransaction();
                evse.clearToken();

                stationMessageSender.sendTransactionEventEnded(evseId, connectorId,
                                                                TriggerReason.EV_DEPARTED,
                                                                Reason.EV_DISCONNECTED,
                                                                evse.getWattConsumedLastSession());
            }
            future.complete(UserMessageResult.SUCCESSFUL);
        });

        stateManager.setStateForEvse(evseId, new AvailableState());
        return future;
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        // NOP
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    @Override
    public CompletableFuture<UserMessageResult> onAuthorize(int evseId, String tokenId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        Evse evse = stationStore.findEvse(evseId);

        log.info("in authorizeToken {}", tokenId);

        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
                evse.setToken(tokenId);
                if (!evse.hasOngoingTransaction()) {
                    String transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
                    evse.createTransaction(transactionId);

                    stationMessageSender.sendTransactionEventStart(evseId, AUTHORIZED, tokenId);
                }

                startCharging(stationMessageSender, evse, tokenId);

                stateManager.setStateForEvse(evseId, new ChargingState());
                future.complete(UserMessageResult.SUCCESSFUL);
            } else {
                future.complete(UserMessageResult.FAILED);
            }
        });
        return future;
    }

    private void startCharging(StationMessageSender stationMessageSender, Evse evse, String tokenId) {
        Integer connectorId = evse.lockPluggedConnector();
        evse.startCharging();
        stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, AUTHORIZED, tokenId, CHARGING);
    }
}
