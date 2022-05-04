package com.evbox.everon.ocpp.simulator.station.evse.states.helpers;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;

import java.util.Optional;

public final class AuthorizeHelper {

    private AuthorizeHelper() {
        //NOP
    }

    public static void handleFailedAuthorizeResponse(StateManager stateManager, Evse evse) {
        if (evse.hasOngoingTransaction()) {
            StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
            StationStore stationStore = stateManager.getStationStore();
            OptionList<TxStartStopPointVariableValues> stopPoints = stationStore.getTxStopPointValues();
            Optional<Connector> connector = evse.tryFindPluggedConnector();

            evse.clearToken();
            evse.stopCharging();

            if (!stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED)) {
                ChargingState chargingState = evse.getTransaction().updateChargingStateIfChanged(ChargingState.SUSPENDED_EVSE);
                stationMessageSender.sendTransactionEventUpdate(evse.getId(),
                        connector.map(Connector::getId).orElse(null),
                        TriggerReason.DEAUTHORIZED,
                        chargingState,
                        evse.getTotalConsumedWattHours());
            } else {
                stationMessageSender.sendTransactionEventEnded(evse.getId(),
                        connector.map(Connector::getId).orElse(null),
                        TriggerReason.DEAUTHORIZED,
                        Reason.DE_AUTHORIZED,
                        evse.getWattConsumedLastSession());
            }
        }
    }
}
