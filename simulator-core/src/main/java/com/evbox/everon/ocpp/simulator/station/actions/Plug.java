package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents Plug message.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class Plug implements UserMessage {

    private final Integer evseId;
    private final Integer connectorId;

    /**
     * Perform Plug-in logic.
     *
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {

        if (stationState.getCableStatus(evseId, connectorId) != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s", connectorId));
        }

        Evse evse = stationState.findEvse(evseId);

        if (!evse.hasOngoingTransaction()) {
            Integer transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
            evse.createTransaction(transactionId);
        }

        evse.plug(connectorId);

        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (statusNotificationRequest, statusNotificationResponse) -> {
            if (evse.hasTokenId()) {
                String tokenId = evse.getTokenId();
                log.info("Station has authorised token {}", tokenId);

                stationMessageSender.sendTransactionEventUpdateAndSubscribe(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, tokenId, TransactionData.ChargingState.EV_DETECTED,
                        (transactionEventRequest, transactionEventResponse) -> {
                            evse.lockPluggedConnector();
                            evse.startCharging();

                            stationMessageSender.sendTransactionEventUpdate(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED, tokenId,
                                    TransactionData.ChargingState.CHARGING);
                        });
            } else {
                stationMessageSender.sendTransactionEventStart(evse.getId(), connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
            }
        });
    }

}
