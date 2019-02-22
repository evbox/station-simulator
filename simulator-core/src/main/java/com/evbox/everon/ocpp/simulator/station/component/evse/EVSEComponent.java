package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class EVSEComponent extends StationComponent {

    public static final String NAME = "EVSE";

    public EVSEComponent(Station station) {
        super(ImmutableList.of(
                new AvailabilityStateVariableAccessor(station),
                new EnabledVariableAccessor(station)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
