package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationPersistenceLayer;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of OCPPCommCtrlr component according to OCPP 2.0 (3.1.7. OCPPCommCtrlr)
 */
public class OCPPCommCtrlrComponent extends StationComponent {

    public static final String NAME = "OCPPCommCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public OCPPCommCtrlrComponent(Station station, StationPersistenceLayer stationPersistenceLayer) {
        super(ImmutableList.of(
                new HeartbeatIntervalVariableAccessor(station, stationPersistenceLayer)
        ));
    }
}
