package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.station.ClearMonitoringResult;
import com.evbox.everon.ocpp.v20.message.station.ClearVariableMonitoringRequest;
import com.evbox.everon.ocpp.v20.message.station.ClearVariableMonitoringResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for {@link ClearVariableMonitoringRequest} request.
 */
@Slf4j
public class ClearVariableMonitoringRequestHandler implements OcppRequestHandler<ClearVariableMonitoringRequest> {

    private StationMessageSender stationMessageSender;
    private StationComponentsHolder stationComponentsHolder;

    public ClearVariableMonitoringRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link ClearVariableMonitoringRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ClearVariableMonitoringRequest request) {
        List<ClearMonitoringResult> results = new ArrayList<>();
        for (int id : request.getId()) {
            ClearMonitoringResult result = new ClearMonitoringResult().withId(id);
            if (stationComponentsHolder.clearMonitor(id)) {
                results.add(result.withStatus(ClearMonitoringResult.Status.ACCEPTED));
            } else {
                results.add(result.withStatus(ClearMonitoringResult.Status.NOT_FOUND));
            }
        }
        stationMessageSender.sendCallResult(callId, new ClearVariableMonitoringResponse().withClearMonitoringResult(results));
    }
}
