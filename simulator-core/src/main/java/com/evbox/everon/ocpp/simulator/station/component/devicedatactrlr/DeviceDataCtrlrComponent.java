package com.evbox.everon.ocpp.simulator.station.component.devicedatactrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.authctrlr.AuthorizeStateVariableAccessor;

import java.util.List;

public class DeviceDataCtrlrComponent extends StationComponent {

    public static final String NAME = "DeviceDataCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public DeviceDataCtrlrComponent(Station station, StationStore stationStore) {
        super(List.of(
                new ItemsPerMessageVariableAccessor(station, stationStore)
        ));
    }
}
