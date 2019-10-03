package com.evbox.everon.ocpp.simulator.station.actions.system;

import com.evbox.everon.ocpp.simulator.station.StationDataHolder;
import com.evbox.everon.ocpp.simulator.station.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.states.WaitingForPlugState;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents request to release the connector for an evse.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class ReleaseConnector implements SystemMessage {

    private final Integer evseId;
    private final Integer connectorId;

    /**
     * Makes the connector specified available again.
     *
     * @param stationDataHolder contains reference to station's managers
     */
    @Override
    public void perform(StationDataHolder stationDataHolder) {
        if (WaitingForPlugState.NAME.equals(stationDataHolder.getStationStateFlowManager().getStateForEvse(evseId).getStateName())) {
            stationDataHolder.getStationMessageSender().sendStatusNotification(evseId, connectorId, StatusNotificationRequest.ConnectorStatus.AVAILABLE);
            stationDataHolder.getStationPersistenceLayer().findEvse(evseId).stopTransaction();
            stationDataHolder.getStationMessageSender().sendTransactionEventEnded(evseId, connectorId, null, TransactionData.StoppedReason.TIMEOUT);
            stationDataHolder.getStationStateFlowManager().setStateForEvse(evseId, new AvailableState());
        }
    }
}
