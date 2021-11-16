package com.evbox.everon.ocpp.simulator.station.component.electricalfield;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class ElectricalFieldComponent extends StationComponent {

    public static final String NAME = "ElectricalFeed";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public ElectricalFieldComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new PVOptimizedChargingVariableAccessor(station, stationStore)
        ));
    }
}
