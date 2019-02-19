package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.exception.UnknownComponentException;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidationResult;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
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

    public SetVariablesRequestHandler(Station station, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = new StationComponentsHolder(station);
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

        List<SetVariableResult> setVariableResults = results.stream().map(validationResult -> {
            SetVariableDatum datum = validationResult.getSetVariableDatum();
            return new SetVariableResult().withAttributeStatus(validationResult.getStatus())
                    .withAttributeType(SetVariableResult.AttributeType.fromValue(datum.getAttributeType().value()))
                    .withVariable(datum.getVariable())
                    .withComponent(datum.getComponent());
        }).collect(toList());

        sendResponse(callId, new SetVariablesResponse().withSetVariableResult(setVariableResults));

        results.stream()
                .filter(SetVariableValidationResult::isAccepted)
                .map(SetVariableValidationResult::getSetVariableDatum)
                .forEach(data -> {
                    String componentName = data.getComponent().getName().toString();
                    Optional<StationComponent> optionalComponent = stationComponentsHolder.getComponent(componentName);
                    StationComponent component = optionalComponent.orElseThrow(() -> new UnknownComponentException(componentName));
                    component.handle(data);
                });
    }

    private List<SetVariableValidationResult> validate(List<SetVariableDatum> setVariableData) {
        return setVariableData.stream().map(data -> {
            String componentName = data.getComponent().getName().toString();
            Optional<StationComponent> optionalComponent = stationComponentsHolder.getComponent(componentName);

            return optionalComponent.map(component -> component.validate(data))
                    .orElse(new SetVariableValidationResult(data, SetVariableResult.AttributeStatus.UNKNOWN_COMPONENT));
        }).collect(toList());
    }

    private void sendResponse(String callId, Object payload) {
        CallResult callResult = new CallResult(callId, payload);
        String callStr = callResult.toJson();
        stationMessageSender.sendMessage(new WebSocketClientInboxMessage.OcppMessage(callStr));
    }
}
