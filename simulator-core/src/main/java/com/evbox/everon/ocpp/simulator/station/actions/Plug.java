package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
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

    private final Integer connectorId;

    /**
     * Perform Plug-in logic.
     *
     * @param stationState state of the station
     * @param stationMessageSender event sender of the station
     */
    @Override
    public void perform(StationState stationState, StationMessageSender stationMessageSender) {

        if (stationState.getCableStatus(connectorId) != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s", connectorId));
        }

        Integer evseId = stationState.findEvseId(connectorId);

        if (!stationState.hasOngoingTransaction(evseId)) {
            Integer transactionId = TransactionIdGenerator.getInstance().getAndIncrement();
            stationState.findEvse(evseId).createTransaction(transactionId);
        }

        stationState.plug(connectorId);

        stationMessageSender.sendStatusNotificationAndSubscribe(evseId, connectorId, (statusNotificationRequest, statusNotificationResponse) -> {
            if (stationState.hasAuthorizedToken(evseId)) {
                String tokenId = stationState.getToken(evseId);
                log.info("Station has authorised token {}", tokenId);

                stationMessageSender.sendTransactionEventUpdateAndSubscribe(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, tokenId, TransactionData.ChargingState.EV_DETECTED,
                        (transactionEventRequest, transactionEventResponse) -> {
                            stationState.lockConnector(evseId);
                            stationState.startCharging(evseId);

                            stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, TransactionEventRequest.TriggerReason.CHARGING_STATE_CHANGED, tokenId,
                                    TransactionData.ChargingState.CHARGING);
                        });
            } else {
                stationMessageSender.sendTransactionEventStart(evseId, connectorId, TransactionEventRequest.TriggerReason.CABLE_PLUGGED_IN, TransactionData.ChargingState.EV_DETECTED);
            }
        });
    }

}
