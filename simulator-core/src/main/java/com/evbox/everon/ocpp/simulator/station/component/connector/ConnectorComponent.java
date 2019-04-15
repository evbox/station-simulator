package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class ConnectorComponent extends StationComponent {

    public static final String NAME = "Connector";

    public ConnectorComponent(Station station, StationState stationState) {
        super(ImmutableList.of(
                new EnabledVariableAccessor(station, stationState),
                new ConnectorTypeVariableAccessor(station, stationState)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
