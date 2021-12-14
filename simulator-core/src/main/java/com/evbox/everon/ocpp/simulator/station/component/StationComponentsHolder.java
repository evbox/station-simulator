package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.authcashectrlr.AuthCacheCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.authctrlr.AuthCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.clockctrlr.ClockCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.devicedatactrlr.DeviceDataCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.electricalfield.ElectricalFieldComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.simulator.station.component.localauthlistctrlr.LocalAuthListCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.sampleddatactrlr.SampledDataCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.securityctrlr.SecurityCtrlrComponent;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxCtrlrComponent;
import com.evbox.everon.ocpp.v201.message.centralserver.ComponentVariable;
import com.evbox.everon.ocpp.v201.message.centralserver.SetMonitoringData;
import com.evbox.everon.ocpp.v201.message.station.ReportData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
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
     *  Value: MonitoredComponents details class
     */
    private final Map<Integer, MonitoredComponent> monitoredComponents;

    public StationComponentsHolder(Station station, StationStore stationStore) {
        List<StationComponent> componentsList = new ImmutableList.Builder<StationComponent>()
                .add(new OCPPCommCtrlrComponent(station, stationStore))
                .add(new ChargingStationComponent(station, stationStore))
                .add(new EVSEComponent(station, stationStore))
                .add(new ConnectorComponent(station, stationStore))
                .add(new SecurityCtrlrComponent(station, stationStore))
                .add(new TxCtrlrComponent(station, stationStore))
                .add(new AuthCtrlrComponent(station, stationStore))
                .add(new AuthCacheCtrlrComponent(station, stationStore))
                .add(new ClockCtrlrComponent(station, stationStore))
                .add(new DeviceDataCtrlrComponent(station, stationStore))
                .add(new ElectricalFieldComponent(station, stationStore))
                .add(new LocalAuthListCtrlrComponent(station, stationStore))
                .add(new SampledDataCtrlrComponent(station, stationStore))
                .build();

        components = ImmutableMap.copyOf(componentsList.stream().collect(
                Collectors.toMap(sc -> new CiString.CiString50(sc.getComponentName()), identity())));
        monitoredComponents = new HashMap<>();
    }

    public Optional<StationComponent> getComponent(CiString.CiString50 componentName) {
        return Optional.ofNullable(components.get(componentName));
    }

    public void monitorComponent(int monitorId, ComponentVariable componentVariable, SetMonitoringData data) {
        MonitoredComponent monitored = monitoredComponents.getOrDefault(monitorId, new MonitoredComponent());
        monitored.addMonitoredComponent(componentVariable, data);
        monitoredComponents.put(monitorId, monitored);
    }

    /**
     * Will collect all the ComponentVariables monitored right now.
     * Because a variable can be monitored by multiple monitors, the result
     * will group all the details regarding a specific ComponentVariable into a list,
     * independently of the monitor to which they belong.
     *
     * @return map with all monitoring details grouped by ComponentVariable
     */
    public Map<ComponentVariable, List<SetMonitoringData>> getAllMonitoredComponents() {
        Map<ComponentVariable, List<SetMonitoringData>> result = new HashMap<>();
        for (Map<ComponentVariable, List<SetMonitoringData>> map : getAllMaps()) {
            map.forEach((key, value) -> result.merge(key, value, (first, second) -> {
                    first.addAll(second);
                    return first;
                }
            ));
        }

        return result;
    }

    /**
     * Will collect all the ComponentVariables monitored right now
     * that has a matching in the input list.
     * Because a variable can be monitored by multiple monitors, the result
     * will group all the details regarding a specific ComponentVariable into a list,
     * independently of the monitor to which they belong.
     *
     * @param componentVariables list of ComponentVariables to match against
     * @return map with all monitoring details grouped by ComponentVariable
     */
    public Map<ComponentVariable, List<SetMonitoringData>> getByComponentAndVariable(List<ComponentVariable> componentVariables) {
        Map<ComponentVariable, List<SetMonitoringData>> result = new HashMap<>();
        for (Map<ComponentVariable, List<SetMonitoringData>> map : getAllMaps()) {
            map.forEach((key, value) -> {
                    if (componentVariables.contains(key)) {
                        result.merge(key, value, (first, second) -> {
                            List<SetMonitoringData> list = new ArrayList<>(first);
                            list.addAll(second);
                            return list;
                        });
                    }
                }
            );
        }

        return result;
    }

    /**
     * Removes all the components currently monitored
     * with the specified id
     *
     * @param id id of the monitor
     * @return true if the monitor exists otherwise false
     */
    public boolean clearMonitor(int id) {
        return nonNull(monitoredComponents.remove(id));
    }

    /**
     * Generates report data for all components in the holder
     *
     * @param onlyMutableVariables if true, returns only those variables that can be set by the operator
     * @return list of {@link ReportData}
     */
    public List<ReportData> generateReportData(boolean onlyMutableVariables) {
        return components
                .values().stream()
                .flatMap(component -> component.generateReportData(onlyMutableVariables).stream())
                .collect(toList());
    }

    private List<Map<ComponentVariable, List<SetMonitoringData>>> getAllMaps() {
        return monitoredComponents.values().stream().map(MonitoredComponent::getMonitoredDetails).collect(toList());
    }


    public static class MonitoredComponent {

        private Map<ComponentVariable, List<SetMonitoringData>> monitoredDetails = new HashMap<>();

        public void addMonitoredComponent(ComponentVariable componentVariable, SetMonitoringData data) {
            List<SetMonitoringData> list = monitoredDetails.getOrDefault(componentVariable, new ArrayList<>());
            list.add(data);
            monitoredDetails.put(componentVariable, list);
        }

        public Map<ComponentVariable, List<SetMonitoringData>> getMonitoredDetails() {
            return monitoredDetails;
        }

    }
}
