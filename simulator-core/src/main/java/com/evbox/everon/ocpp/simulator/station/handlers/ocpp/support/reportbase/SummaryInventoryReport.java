package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.ACCEPTED;

@Slf4j
public class SummaryInventoryReport extends BaseReport {

    private static final String AVAILABILITY_STATE_NAME = "AvailabilityState";

    private final StationComponentsHolder stationComponentsHolder;

    public SummaryInventoryReport(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender, Clock clock) {
        super(stationMessageSender, clock);
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
        List<ReportDatum> reportData = new ArrayList<>();

        reportData.addAll(getAvailabilityStateReport(ChargingStationComponent.NAME));
        reportData.addAll(getAvailabilityStateReport(EVSEComponent.NAME));
        reportData.addAll(getAvailabilityStateReport(ConnectorComponent.NAME));

        try {
            log.info("Summary inventory base report: \n {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(reportData));
        } catch (JsonProcessingException e) {
            log.info("Error printing the summary inventory base report.", e);
        }
        sendNotifyReportRequests(reportData, request);
    }

    private List<ReportDatum> getAvailabilityStateReport(String componentName) {
        Optional<StationComponent> optComponent = stationComponentsHolder.getComponent(new CiString.CiString50(componentName));
        List<ReportDatum> reportData = new ArrayList<>();
        if (optComponent.isPresent()) {
            StationComponent component = optComponent.get();
            reportData.addAll(component.getVariableAccessorByName(new CiString.CiString50(AVAILABILITY_STATE_NAME)).generateReportData(component.getComponentName()));
        }
        return reportData;
    }
}
