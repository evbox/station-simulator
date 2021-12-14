package com.evbox.everon.ocpp.simulator.station.component.authcashectrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class AuthCacheCtrlrComponent extends StationComponent {
    public static final String NAME = "AuthCacheCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public AuthCacheCtrlrComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new EnabledVariableAccessor(station, stationStore),
                new LifeTimeVariableAccessor(station, stationStore)
        ));
    }

}
