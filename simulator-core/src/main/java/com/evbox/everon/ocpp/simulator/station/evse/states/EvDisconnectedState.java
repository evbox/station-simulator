package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;

import java.util.concurrent.CompletableFuture;

import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.REMOTE_STOP;

public class EvDisconnectedState extends AbstractEvseState {

    public static final String NAME = "EV_DISCONNECTED";

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
        return CompletableFuture.completedFuture(UserMessageResult.NOT_EXECUTED);
    }

    @Override
    public CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId) {
        stopChargingTransaction(evseId, connectorId);

        stateManager.setStateForEvse(evseId, new AvailableState());
        return CompletableFuture.completedFuture(UserMessageResult.SUCCESSFUL);
    }

    private void stopChargingTransaction(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        Evse evse = stationStore.findEvse(evseId);

        evse.stopCharging();
        evse.tryUnlockConnector();
        evse.unplug(connectorId);
        evse.stopTransaction();
        evse.clearToken();

        stationMessageSender.sendTransactionEventEnded(evseId, connectorId,
                TriggerReason.EV_COMMUNICATION_LOST,
                Reason.EV_DISCONNECTED,
                evse.getWattConsumedLastSession());
        stationMessageSender.sendStatusNotification(evse, evse.findConnector(connectorId));
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        //NOP
    }

    @Override
    public void onRemoteStop(int evseId) {
        Evse evse = stateManager.getStationStore().findEvse(evseId);
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        evse.stopCharging();
        Integer connectorId = evse.tryUnlockConnector();
        stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, REMOTE_STOP, com.evbox.everon.ocpp.v201.message.station.ChargingState.EV_CONNECTED);

        stateManager.setStateForEvse(evseId, new RemotelyStoppedState());
    }
}
