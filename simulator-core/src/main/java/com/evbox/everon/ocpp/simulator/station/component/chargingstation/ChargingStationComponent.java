package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of ChargingStation component according to OCPP 2.0 (3.2.12. ChargingStation)
 */
public class ChargingStationComponent extends StationComponent {

    public static final String NAME = "ChargingStation";

    public ChargingStationComponent(Station station, StationState stationState) {
        super(ImmutableList.of(
                new IdentityVariableAccessor(station, stationState),
                new ManufacturerVariableAccessor(station, stationState),
                new ModelVariableAccessor(station, stationState),
                new SerialNumberVariableAccessor(station, stationState),
                new ChargeProtocolVariableAccessor(station, stationState)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
