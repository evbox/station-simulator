package com.evbox.everon.ocpp.simulator.station.component.authctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;

import java.util.List;

/**
 * Representation of AuthCtrlr component according to OCPP 2.0.1 - Responsible for configuration relating to the use of authorization for Charging Station use.
 */
public class AuthCtrlrComponent extends StationComponent {

    public static final String NAME = "AuthCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public AuthCtrlrComponent(Station station, StationStore stationStore) {
        super(List.of(
                new AuthorizeStateVariableAccessor(station, stationStore)
        ));
    }
}
