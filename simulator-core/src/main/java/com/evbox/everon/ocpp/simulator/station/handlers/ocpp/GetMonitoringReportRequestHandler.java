package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v201.message.centralserver.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.evbox.everon.ocpp.v201.message.centralserver.Monitor.PERIODIC;
import static com.evbox.everon.ocpp.v201.message.centralserver.Monitor.PERIODIC_CLOCK_ALIGNED;

public class GetMonitoringReportRequestHandler implements OcppRequestHandler<GetMonitoringReportRequest> {

    private StationMessageSender stationMessageSender;
    private StationComponentsHolder stationComponentsHolder;

    public GetMonitoringReportRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link GetMonitoringReportRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, GetMonitoringReportRequest request) {
        if (request.getMonitoringCriteria() != null && request.getMonitoringCriteria().contains(MonitoringCriterion.PERIODIC_MONITORING)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatus.NOT_SUPPORTED));
            return;
        }

        Map<ComponentVariable, List<SetMonitoringData>> monitoredComponents;
        if (request.getComponentVariable() != null && !request.getComponentVariable().isEmpty()) {
            monitoredComponents = stationComponentsHolder.getByComponentAndVariable(request.getComponentVariable());
        } else {
            monitoredComponents = stationComponentsHolder.getAllMonitoredComponents();
        }

        // Filter by type of monitor
        Set<Monitor> requestedType = convertCriteriaToMonitorType(Optional.ofNullable(request.getMonitoringCriteria()).orElseGet(ArrayList::new));
        monitoredComponents.replaceAll((k, v) -> v.stream().filter(d -> requestedType.contains(d.getType())).collect(Collectors.toList()));

        if (monitoredComponents.isEmpty() || monitoredComponents.values().stream().allMatch(List::isEmpty)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatus.REJECTED));
        } else {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatus.ACCEPTED));
            stationMessageSender.sendNotifyMonitoringReport(request.getRequestId(), monitoredComponents);
        }
    }

    private EnumSet<Monitor> convertCriteriaToMonitorType(List<MonitoringCriterion> criterion) {
        EnumSet<Monitor> criteriaToRemove = EnumSet.of(PERIODIC, PERIODIC_CLOCK_ALIGNED);

        if (!criterion.isEmpty()) {
            if (!criterion.contains(MonitoringCriterion.THRESHOLD_MONITORING)) {
                criteriaToRemove.add(Monitor.UPPER_THRESHOLD);
                criteriaToRemove.add(Monitor.LOWER_THRESHOLD);
            }

            if (!criterion.contains(MonitoringCriterion.DELTA_MONITORING)) {
                criteriaToRemove.add(Monitor.DELTA);
            }
        }

        return EnumSet.complementOf(criteriaToRemove);
    }
}
