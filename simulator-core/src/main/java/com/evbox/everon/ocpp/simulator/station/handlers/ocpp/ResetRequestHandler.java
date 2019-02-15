package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.ResetResponse;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.TriggerReason.REMOTE_STOP;

/**
 * Handler for {@link ResetRequest} request.
 */
@Slf4j
@AllArgsConstructor
public class ResetRequestHandler implements OcppRequestHandler<ResetRequest> {

    private final StationState state;
    private final StationMessageSender stationMessageSender;

    /**
     * Handle {@link ResetRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ResetRequest request) {
        if (request.getType() == ResetRequest.Type.IMMEDIATE) {
            sendResponse(callId, new ResetResponse().withStatus(ResetResponse.Status.ACCEPTED));
            resetStation();
        } else {
            sendResponse(callId, new ResetResponse().withStatus(ResetResponse.Status.REJECTED));
        }
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.OcppMessage(callStr));
    }

    void resetStation() {
        List<Integer> evseIds = state.getEvses();
        evseIds.forEach(evseId -> {
            if (state.hasOngoingTransaction(evseId)) {
                state.stopCharging(evseId);
                Integer connectorId = state.unlockConnector(evseId);
                stationMessageSender.sendTransactionEventEndedAndSubscribe(evseId, connectorId, REMOTE_STOP, TransactionData.StoppedReason.IMMEDIATE_RESET, (request, response) -> {
                    reboot();
                });
            } else {
                reboot();
            }
        });
    }

    private void reboot() {
        state.clearTokens();
        state.clearTransactions();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.Disconnect());
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.Connect());
        stationMessageSender.sendBootNotification(BootNotificationRequest.Reason.REMOTE_RESET);
    }
}
