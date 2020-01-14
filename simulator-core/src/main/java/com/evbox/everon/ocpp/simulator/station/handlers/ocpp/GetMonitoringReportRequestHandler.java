package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.centralserver.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.evbox.everon.ocpp.v20.message.centralserver.SetMonitoringDatum.Type.PERIODIC;
import static com.evbox.everon.ocpp.v20.message.centralserver.SetMonitoringDatum.Type.PERIODIC_CLOCK_ALIGNED;

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
        if (request.getMonitoringCriteria() != null && request.getMonitoringCriteria().contains(MonitoringCriterium.PERIODIC_MONITORING)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GetMonitoringReportResponse.Status.NOT_SUPPORTED));
            return;
        }

        Map<ComponentVariable, List<SetMonitoringDatum>> monitoredComponents;
        if (request.getComponentVariable() != null && !request.getComponentVariable().isEmpty()) {
            monitoredComponents = stationComponentsHolder.getByComponentAndVariable(request.getComponentVariable());
        } else {
            monitoredComponents = stationComponentsHolder.getAllMonitoredComponents();
        }

        // Filter by type of monitor
        Set<SetMonitoringDatum.Type> requestedType = convertCriteriaToMonitorType(Optional.ofNullable(request.getMonitoringCriteria()).orElseGet(ArrayList::new));
        monitoredComponents.replaceAll((k, v) -> v.stream().filter(d -> requestedType.contains(d.getType())).collect(Collectors.toList()));

        if (monitoredComponents.isEmpty() || monitoredComponents.values().stream().allMatch(List::isEmpty)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GetMonitoringReportResponse.Status.REJECTED));
        } else {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GetMonitoringReportResponse.Status.ACCEPTED));
            stationMessageSender.sendNotifyMonitoringReport(request.getRequestId(), monitoredComponents);
        }
    }

    private EnumSet<SetMonitoringDatum.Type> convertCriteriaToMonitorType(List<MonitoringCriterium> criterion) {
        EnumSet<SetMonitoringDatum.Type> criteriaToRemove = EnumSet.of(PERIODIC, PERIODIC_CLOCK_ALIGNED);

        if (!criterion.isEmpty()) {
            if (!criterion.contains(MonitoringCriterium.THRESHOLD_MONITORING)) {
                criteriaToRemove.add(SetMonitoringDatum.Type.UPPER_THRESHOLD);
                criteriaToRemove.add(SetMonitoringDatum.Type.LOWER_THRESHOLD);
            }

            if (!criterion.contains(MonitoringCriterium.DELTA_MONITORING)) {
                criteriaToRemove.add(SetMonitoringDatum.Type.DELTA);
            }
        }

        return EnumSet.complementOf(criteriaToRemove);
    }
}
