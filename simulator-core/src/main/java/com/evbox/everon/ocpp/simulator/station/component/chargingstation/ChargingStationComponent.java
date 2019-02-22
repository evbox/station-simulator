package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of ChargingStation component according to OCPP 2.0 (3.2.12. ChargingStation)
 */
public class ChargingStationComponent extends StationComponent {

    public static final String NAME = "ChargingStation";

    public ChargingStationComponent(Station station) {
        super(ImmutableList.of(
                new IdentityVariableAccessor(station),
                new ManufacturerVariableAccessor(station),
                new ModelVariableAccessor(station),
                new SerialNumberVariableAccessor(station),
                new ChargeProtocolVariableAccessor(station)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
