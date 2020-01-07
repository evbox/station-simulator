package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.securityctrlr.SecurityCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxCtrlrComponent;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;
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
    private final Map<CiString.CiString50, StationComponent> components;

    /**
     *  Keeps track for each component which variables are monitored.
     *  Key: Id of the monitor
     *  Value: Map that for each monitor has a set of names of monitored variables
     */
    private final Map<Integer, Map<String, Set<String>>> monitoredComponents;

    public StationComponentsHolder(Station station, StationStore stationStore) {
        List<StationComponent> componentsList = new ImmutableList.Builder<StationComponent>()
                .add(new OCPPCommCtrlrComponent(station, stationStore))
                .add(new ChargingStationComponent(station, stationStore))
                .add(new EVSEComponent(station, stationStore))
                .add(new ConnectorComponent(station, stationStore))
                .add(new SecurityCtrlrComponent(station, stationStore))
                .add(new TxCtrlrComponent(station, stationStore))
                .build();

        components = ImmutableMap.copyOf(componentsList.stream().collect(
                Collectors.toMap(sc -> new CiString.CiString50(sc.getComponentName()), identity())));
        monitoredComponents = new HashMap<>();
    }

    public Optional<StationComponent> getComponent(CiString.CiString50 componentName) {
        return Optional.ofNullable(components.get(componentName));
    }

    public void monitorComponent(int monitorId, String componentName, String variableName) {
        Map<String, Set<String>> map = monitoredComponents.getOrDefault(monitorId, new HashMap<>());
        Set<String> set = map.getOrDefault(componentName, new HashSet<>());
        set.add(variableName);
        map.put(componentName, set);
        monitoredComponents.put(monitorId, map);
    }

    public boolean clearMonitor(int id) {
        if (monitoredComponents.containsKey(id)) {
            monitoredComponents.remove(id);
            return true;
        }
        return false;
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
