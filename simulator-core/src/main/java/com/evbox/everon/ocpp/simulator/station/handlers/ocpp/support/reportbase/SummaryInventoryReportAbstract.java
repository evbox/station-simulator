package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v201.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v201.message.station.ReportData;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.evbox.everon.ocpp.v201.message.station.GenericDeviceModelStatus.ACCEPTED;

public class SummaryInventoryReportAbstract extends AbstractBaseReport {

    private static final String AVAILABILITY_STATE_NAME = "AvailabilityState";

    private final StationComponentsHolder stationComponentsHolder;

    public SummaryInventoryReportAbstract(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender, Clock clock) {
        super(stationMessageSender, clock);
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
        List<ReportData> reportData = new ArrayList<>();

        reportData.addAll(getAvailabilityStateReport(ChargingStationComponent.NAME));
        reportData.addAll(getAvailabilityStateReport(EVSEComponent.NAME));
        reportData.addAll(getAvailabilityStateReport(ConnectorComponent.NAME));

        sendNotifyReportRequests(reportData, request);
    }

    private List<ReportData> getAvailabilityStateReport(String componentName) {
        Optional<StationComponent> optComponent = stationComponentsHolder.getComponent(new CiString.CiString50(componentName));
        List<ReportData> reportData = new ArrayList<>();
        if (optComponent.isPresent()) {
            StationComponent component = optComponent.get();
            reportData.addAll(component.getVariableAccessorByName(AVAILABILITY_STATE_NAME).generateReportData(component.getComponentName()));
        }
        return reportData;
    }
}
