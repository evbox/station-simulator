package com.evbox.everon.ocpp.simulator.station.component.securityctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class SecurityCtrlrComponent extends StationComponent {

    private static final String NAME = "SecurityCtrlr";

    public SecurityCtrlrComponent(Station station, StationPersistenceLayer stationPersistenceLayer) {
        super(ImmutableList.of(new BasicAuthPasswordVariableAccessor(station, stationPersistenceLayer)));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
