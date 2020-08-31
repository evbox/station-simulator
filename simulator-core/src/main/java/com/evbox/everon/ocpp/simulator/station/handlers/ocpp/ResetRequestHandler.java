package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetResponse;
import com.evbox.everon.ocpp.v201.message.centralserver.ResetStatus;
import com.evbox.everon.ocpp.v201.message.station.BootReason;
import com.evbox.everon.ocpp.v201.message.station.Reason;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.evbox.everon.ocpp.v201.message.station.TriggerReason.REMOTE_STOP;

/**
 * Handler for {@link ResetRequest} request.
 */
@Slf4j
@AllArgsConstructor
public class ResetRequestHandler implements OcppRequestHandler<ResetRequest> {

    private final StationStore state;
    private final StationMessageSender stationMessageSender;

    /**
     * Handle {@link ResetRequest} request.
     *
     * @param callId  identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ResetRequest request) {
        sendResponse(callId, new ResetResponse().withStatus(ResetStatus.ACCEPTED));
        resetStation();

    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new AbstractWebSocketClientInboxMessage.OcppMessage(callStr));
    }

    void resetStation() {
        List<Integer> evseIds = state.getEvseIds();
        evseIds.forEach(evseId -> {
            if (state.hasOngoingTransaction(evseId)) {
                state.stopCharging(evseId);
                long powerConsumed = state.findEvse(evseId).getWattConsumedLastSession();
                Integer connectorId = state.unlockConnector(evseId);
                stationMessageSender.sendTransactionEventEndedAndSubscribe(evseId, connectorId, REMOTE_STOP, Reason.IMMEDIATE_RESET, powerConsumed, (request, response) -> reboot());
            } else {
                reboot();
            }
        });
    }

    private void reboot() {
        state.clearTokens();
        state.clearTransactions();
        stationMessageSender.sendMessage(new AbstractWebSocketClientInboxMessage.Disconnect());
        stationMessageSender.sendMessage(new AbstractWebSocketClientInboxMessage.Connect());
        stationMessageSender.sendBootNotification(BootReason.REMOTE_RESET);
    }
}
