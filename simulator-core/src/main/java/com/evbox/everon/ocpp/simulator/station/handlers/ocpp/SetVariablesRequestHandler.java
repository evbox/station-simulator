package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Handler for {@link SetVariablesRequest} request.
 */
@Slf4j
@AllArgsConstructor
public class SetVariablesRequestHandler implements OcppRequestHandler<SetVariablesRequest> {

    private final StationMessageSender stationMessageSender;

    /**
     * Handle {@link SetVariablesRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, SetVariablesRequest request) {
        List<SetVariableResult> results = request.getSetVariableData()
                .stream()
                .map(variableData -> new SetVariableResult().withComponent(variableData.getComponent())
                        .withVariable(variableData.getVariable())
                        .withAttributeStatus(SetVariableResult.AttributeStatus.REJECTED))
                .collect(toList());

        sendResponse(callId, new SetVariablesResponse().withSetVariableResult(results));
    }


    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.OcppMessage(callStr));
    }
}
