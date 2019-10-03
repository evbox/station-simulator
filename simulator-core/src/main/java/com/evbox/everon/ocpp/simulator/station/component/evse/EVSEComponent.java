package com.evbox.everon.ocpp.simulator.station.component.evse;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of EVSE component according to OCPP 2.0 (3.2.30. EVSE)
 */
public class EVSEComponent extends StationComponent {

    public static final String NAME = "EVSE";

    public EVSEComponent(Station station, StationPersistenceLayer stationPersistenceLayer) {
        super(ImmutableList.of(
                new AvailabilityStateVariableAccessor(station, stationPersistenceLayer),
                new EnabledVariableAccessor(station, stationPersistenceLayer)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
