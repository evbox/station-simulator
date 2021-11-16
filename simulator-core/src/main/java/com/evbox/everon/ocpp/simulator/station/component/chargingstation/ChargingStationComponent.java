package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of ChargingStation component according to OCPP 2.0 (3.2.12. ChargingStation)
 */
public class ChargingStationComponent extends StationComponent {

    public static final String NAME = "ChargingStation";

    public ChargingStationComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new IdentityVariableAccessor(station, stationStore),
                new ManufacturerVariableAccessor(station, stationStore),
                new ModelVariableAccessor(station, stationStore),
                new SerialNumberVariableAccessor(station, stationStore),
                new ChargeProtocolVariableAccessor(station, stationStore),
                new AvailabilityStateVariableAccessor(station, stationStore),
                new VendorNameVariableAccessor(station, stationStore)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
