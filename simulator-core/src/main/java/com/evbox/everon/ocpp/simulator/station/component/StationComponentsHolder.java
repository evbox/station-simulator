package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.chargingstation.ChargingStationComponent;
import com.evbox.everon.ocpp.simulator.station.component.connector.ConnectorComponent;
import com.evbox.everon.ocpp.simulator.station.component.evse.EVSEComponent;
import com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr.OCPPCommCtrlrComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class StationComponentsHolder {

    private Map<String, StationComponent> components;

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
}
