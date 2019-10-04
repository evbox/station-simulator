package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class ConnectorComponent extends StationComponent {

    public static final String NAME = "Connector";

    public ConnectorComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new EnabledVariableAccessor(station, stationStore),
                new ConnectorTypeVariableAccessor(station, stationStore)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
