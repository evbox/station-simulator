package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.*;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static java.util.stream.Collectors.toList;

@FieldDefaults(makeFinal = true)
public class RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private Station station;
    private BlockingQueue<WebSocketClientInboxMessage> webSocketInbox;

    public RequestHandler(Station station, BlockingQueue<WebSocketClientInboxMessage> webSocketInbox) {
        this.station = station;
        this.webSocketInbox = webSocketInbox;
    }

    public void handle(String callId, GetVariablesRequest request) {
        List<GetVariableResult> results = request.getGetVariableData()
                .stream()
                .map(variableData -> new GetVariableResult().withComponent(variableData.getComponent())
                        .withVariable(variableData.getVariable())
                        .withAttributeStatus(GetVariableResult.AttributeStatus.REJECTED))
                .collect(toList());

        sendResponse(callId, new GetVariablesResponse().withGetVariableResult(results));
    }

    public void handle(String callId, SetVariablesRequest request) {
        List<SetVariableResult> results = request.getSetVariableData()
                .stream()
                .map(variableData -> new SetVariableResult().withComponent(variableData.getComponent())
                        .withVariable(variableData.getVariable())
                        .withAttributeStatus(SetVariableResult.AttributeStatus.REJECTED))
                .collect(toList());

        sendResponse(callId, new SetVariablesResponse().withSetVariableResult(results));
    }

    public void handle(String callId, ResetRequest request) {
        if (request.getType() == ResetRequest.Type.IMMEDIATE) {
            sendResponse(callId, new ResetResponse().withStatus(ResetResponse.Status.ACCEPTED));
            station.resetStation();
        } else {
            sendResponse(callId, new ResetResponse().withStatus(ResetResponse.Status.REJECTED));
        }
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        try {
            webSocketInbox.put(new WebSocketClientInboxMessage.OcppMessage(callStr));
        } catch (InterruptedException e) {
            LOGGER.error("Exception on adding message to WebSocketInbox", e);
            Thread.currentThread().interrupt();
        }
    }
}
