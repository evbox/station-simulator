package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

/**
 * Contains station components (OCPP 2.0 Appendix 3. Standardized Components) that are supported at the moment
 */
public class StationComponentsHolder {

    /**
     * Maps component name to its implementation where each of them has variables
     */
    private final Map<String, StationComponent> components;

    public StationComponentsHolder(Station station) {
        List<StationComponent> componentsList = new ImmutableList.Builder<StationComponent>()
                .add(new OCPPCommCtrlrComponent(station))
                .add(new ChargingStationComponent(station))
                .add(new EVSEComponent(station))
                .add(new ConnectorComponent(station))
                .build();

        components = ImmutableMap.copyOf(componentsList.stream().collect(Collectors.toMap(StationComponent::getComponentName, identity())));
    }

    public Optional<StationComponent> getComponent(String componentName) {
        return Optional.ofNullable(components.get(componentName));
    }

    /**
     * Generates report data for all components in the holder
     *
     * @param onlyMutableVariables if true, returns only those variables that can be set by the operator
     * @return list of {@link ReportDatum}
     */
    public List<ReportDatum> generateReportData(boolean onlyMutableVariables) {
        return components
                .values().stream()
                .flatMap(component -> component.generateReportData(onlyMutableVariables).stream())
                .collect(toList());
    }
}
