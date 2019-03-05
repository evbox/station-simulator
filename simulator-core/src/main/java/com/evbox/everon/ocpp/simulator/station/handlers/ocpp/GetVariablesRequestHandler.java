package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.GetVariablesResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Handler for {@link GetVariablesRequest} request.
 */
@Slf4j
public class GetVariablesRequestHandler implements OcppRequestHandler<GetVariablesRequest> {

    private static final GetVariableResult UNKNOWN_COMPONENT = new GetVariableResult().withAttributeStatus(GetVariableResult.AttributeStatus.UNKNOWN_COMPONENT);

    private final StationMessageSender stationMessageSender;
    private final StationComponentsHolder stationComponentsHolder;

    public GetVariablesRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link GetVariablesRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, GetVariablesRequest request) {
        List<GetVariableResult> results = request.getGetVariableData().stream().map(data -> {
            String componentName = data.getComponent().getName().toString();

            return stationComponentsHolder.getComponent(componentName)
                    .map(stationComponent -> stationComponent.getVariable(data))
                    .orElse(UNKNOWN_COMPONENT);
        }).collect(toList());

        sendResponse(callId, new GetVariablesResponse().withGetVariableResult(results));
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.OcppMessage(callStr));
    }
}
