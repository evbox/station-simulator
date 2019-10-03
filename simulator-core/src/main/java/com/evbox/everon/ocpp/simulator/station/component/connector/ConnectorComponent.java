package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class ConnectorComponent extends StationComponent {

    public static final String NAME = "Connector";

    public ConnectorComponent(Station station, StationPersistenceLayer stationPersistenceLayer) {
        super(ImmutableList.of(
                new EnabledVariableAccessor(station, stationPersistenceLayer),
                new ConnectorTypeVariableAccessor(station, stationPersistenceLayer)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
