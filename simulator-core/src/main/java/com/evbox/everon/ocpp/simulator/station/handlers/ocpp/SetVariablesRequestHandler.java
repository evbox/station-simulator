package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.exception.UnknownComponentException;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableDatum;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.centralserver.SetVariablesResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Handler for {@link SetVariablesRequest} request.
 */
@Slf4j
public class SetVariablesRequestHandler implements OcppRequestHandler<SetVariablesRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationComponentsHolder stationComponentsHolder;

    public SetVariablesRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link SetVariablesRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, SetVariablesRequest request) {
        List<SetVariableDatum> setVariableData = request.getSetVariableData();

        List<SetVariableValidationResult> results = validate(setVariableData);

        List<SetVariableResult> setVariableResults = results.stream()
                .map(SetVariableValidationResult::getResult)
                .collect(toList());

        sendResponse(callId, new SetVariablesResponse().withSetVariableResult(setVariableResults));

        results.stream()
                .filter(SetVariableValidationResult::isAccepted)
                .map(SetVariableValidationResult::getSetVariableDatum)
                .forEach(data -> {
                    CiString.CiString50 componentName = data.getComponent().getName();
                    Optional<StationComponent> optionalComponent = stationComponentsHolder.getComponent(data.getComponent().getName());
                    StationComponent component = optionalComponent.orElseThrow(() -> new UnknownComponentException(componentName.toString()));
                    component.setVariable(data);
                });
    }

    private List<SetVariableValidationResult> validate(List<SetVariableDatum> setVariableData) {
        return setVariableData.stream().map(data -> {
            CiString.CiString50 componentName = data.getComponent().getName();
            Optional<StationComponent> optionalComponent = stationComponentsHolder.getComponent(componentName);

            return optionalComponent.map(component -> component.validate(data))
                    .orElse(new SetVariableValidationResult(data, new SetVariableResult()
                            .withComponent(data.getComponent())
                            .withVariable(data.getVariable())
                            .withAttributeType(SetVariableResult.AttributeType.fromValue(data.getAttributeType().value()))
                            .withAttributeStatus(SetVariableResult.AttributeStatus.UNKNOWN_COMPONENT)
                    ));
        }).collect(toList());
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new AbstractWebSocketClientInboxMessage.OcppMessageAbstract(callStr));
    }
}
