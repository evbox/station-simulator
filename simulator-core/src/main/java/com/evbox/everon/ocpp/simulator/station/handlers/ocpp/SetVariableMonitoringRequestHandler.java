package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handler for {@link SetVariableMonitoringRequest} request.
 */
@Slf4j
public class SetVariableMonitoringRequestHandler implements OcppRequestHandler<SetVariableMonitoringRequest> {

    private StationComponentsHolder stationComponentsHolder;
    private StationMessageSender stationMessageSender;

    public SetVariableMonitoringRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationComponentsHolder = stationComponentsHolder;
        this.stationMessageSender = stationMessageSender;
    }

    /**
     * Handle {@link SetVariableMonitoringRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, SetVariableMonitoringRequest request) {
        List<SetMonitoringResult> results = new ArrayList<>();

        for (SetMonitoringData data : request.getSetMonitoringData()) {
            SetMonitoringResult monitoringResult = buildResponse(data);

            String componentName = data.getComponent().getName().toString();
            Optional<StationComponent> component = stationComponentsHolder.getComponent(new CiString.CiString50(componentName));

            component.ifPresent(c -> {
                String variableName = data.getVariable().getName().toString();
                if (!c.getVariableNames().contains(variableName)) {
                    monitoringResult.setStatus(SetMonitoringStatus.UNKNOWN_VARIABLE);
                } else {
                    int id = Optional.ofNullable(data.getId()).orElseGet(() -> ThreadLocalRandom.current().nextInt());
                    stationComponentsHolder.monitorComponent(id, buildComponentVariable(componentName, variableName), data);
                    monitoringResult.setId(id);
                    monitoringResult.setStatus(SetMonitoringStatus.ACCEPTED);
                }
            });

            results.add(monitoringResult);
        }

        stationMessageSender.sendCallResult(callId, new SetVariableMonitoringResponse().withSetMonitoringResult(results));
    }

    private ComponentVariable buildComponentVariable(String componentName, String variableName) {
        return new ComponentVariable().withComponent(new Component().withName(new CiString.CiString50(componentName)))
                                        .withVariable(new Variable().withName(new CiString.CiString50(variableName)));
    }

    private SetMonitoringResult buildResponse(SetMonitoringData request) {
        return new SetMonitoringResult()
                .withStatus(SetMonitoringStatus.UNKNOWN_COMPONENT)
                .withType(Monitor.fromValue(request.getType().value()))
                .withSeverity(request.getSeverity())
                .withComponent(request.getComponent())
                .withVariable(request.getVariable());
    }
}
