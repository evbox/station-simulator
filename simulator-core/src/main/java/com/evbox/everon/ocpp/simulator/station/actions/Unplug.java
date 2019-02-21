package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.evse.ConnectorStatus;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationResponse;
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
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {

        if (stationState.getConnectorState(connectorId) == ConnectorStatus.LOCKED) {
            throw new IllegalStateException("Unable to unplug locked connector: " + connectorId);
        }

        Integer evseId = stationState.findEvseId(connectorId);
        stationState.unplug(connectorId);

        stationMessageSender.sendStatusNotificationAndSubscribe(evseId, connectorId, (Subscriber<StatusNotificationRequest, StatusNotificationResponse>) (request, response) -> {
            stationMessageSender.sendTransactionEventEnded(evseId, connectorId, TransactionEventRequest.TriggerReason.EV_DEPARTED, TransactionData.StoppedReason.EV_DISCONNECTED);
            stationState.clearToken(evseId);
            stationState.clearTransactionId(evseId);
        });
    }
}
