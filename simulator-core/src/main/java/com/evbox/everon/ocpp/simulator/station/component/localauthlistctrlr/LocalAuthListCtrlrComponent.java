package com.evbox.everon.ocpp.simulator.station.component.localauthlistctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class LocalAuthListCtrlrComponent extends StationComponent {

    public static final String NAME = "LocalAuthListCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public LocalAuthListCtrlrComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new EnabledVariableAccessor(station, stationStore),
                new EntriesVariableAccessor(station, stationStore),
                new ItemsPerMessageVariableAccessor(station, stationStore),
                new BytesPerMessageVariableAccessor(station, stationStore)
        ));
    }
}
