package com.evbox.everon.ocpp.simulator.station.component.securityctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class SecurityCtrlrComponent extends StationComponent {

    private static final String NAME = "SecurityCtrlr";

    public SecurityCtrlrComponent(Station station, StationState stationState) {
        super(ImmutableList.of(new BasicAuthPasswordVariableAccessor(station, stationState)));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
