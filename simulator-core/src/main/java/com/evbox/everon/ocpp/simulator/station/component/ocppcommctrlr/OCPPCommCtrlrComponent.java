package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class OCPPCommCtrlrComponent extends StationComponent {

    public static final String NAME = "OCPPCommCtrlr";

    public OCPPCommCtrlrComponent(Station station) {
        super(ImmutableList.of(
                new HeartbeatIntervalVariableAccessor(station),
                new WebSocketPingIntervalVariableAccessor(station)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
