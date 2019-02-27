package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents Unplug message.
 */
@Getter
@AllArgsConstructor
public class Unplug implements UserMessage {

    private final Integer connectorId;

    /**
     * Perform unplug logic.
     *
     * @param stationState         state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {

        if (stationState.getCableStatus(connectorId) == CableStatus.LOCKED) {
            throw new IllegalStateException("Unable to unplug locked connector: " + connectorId);
        }

        Evse evse = stationState.findEvseByConnectorId(connectorId);
        evse.tryUnplug(connectorId);

        stationMessageSender.sendStatusNotificationAndSubscribe(evse.getId(), connectorId, (request, response) -> {
            stationMessageSender.sendTransactionEventEnded(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);

            evse.clearToken();
            evse.stopTransaction();
        });
    }
}
