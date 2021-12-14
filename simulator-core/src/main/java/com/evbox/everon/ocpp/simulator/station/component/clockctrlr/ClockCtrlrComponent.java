package com.evbox.everon.ocpp.simulator.station.component.clockctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.authctrlr.AuthorizeStateVariableAccessor;

import java.util.List;

public class ClockCtrlrComponent extends StationComponent {

    public static final String NAME = "ClockCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public ClockCtrlrComponent(Station station, StationStore stationStore) {
        super(List.of(
                new TimeSourceVariableAccessor(station, stationStore),
                new DateTimeVariableAccessor(station, stationStore)
        ));
    }
}
