package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Handler for {@link GetVariablesRequest} request.
 */
@Slf4j
@AllArgsConstructor
public class GetVariablesRequestHandler implements OcppRequestHandler<GetVariablesRequest> {

    private final StationMessageSender stationMessageSender;

    /**
     * Handle {@link GetVariablesRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, GetVariablesRequest request) {
        List<GetVariableResult> results = request.getGetVariableData()
                .stream()
                .map(variableData -> new GetVariableResult().withComponent(variableData.getComponent())
                        .withVariable(variableData.getVariable())
                        .withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED))
                .collect(toList());

        sendResponse(callId, new GetVariablesResponse().withGetVariableResult(results));
    }


    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.OcppMessage(callStr));
    }
}
