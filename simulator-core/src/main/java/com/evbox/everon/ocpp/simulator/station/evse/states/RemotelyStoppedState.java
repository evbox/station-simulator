package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;

public class RemotelyStoppedState extends StoppedState {

    public static final String NAME = "REMOTELY_STOPPED";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public void onUnplug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        Evse evse = stationStore.findEvse(evseId);

        evse.unplug(connectorId);
        evse.stopTransaction();
        evse.clearToken();

        stationMessageSender.sendStatusNotification(evse, evse.findConnector(connectorId));
        stationMessageSender.sendTransactionEventEnded(evseId, connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.REMOTE);

        stateManager.setStateForEvse(evseId, new AvailableState());
    }
}
