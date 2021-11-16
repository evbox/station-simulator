package com.evbox.everon.ocpp.simulator.station.component.sampleddatactrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

public class SampledDataCtrlrComponent extends StationComponent {

    public static final String NAME = "SampledDataCtrlr";

    @Override
    public String getComponentName() {
        return NAME;
    }

    public SampledDataCtrlrComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new TxEndedMeasurandsVariableAccessor(station, stationStore),
                new TxStartedMeasurandsVariableAccessor(station, stationStore),
                new TxUpdatedMeasurandsVariableAccessor(station, stationStore),
                new TxEndedIntervalVariableAccessor(station, stationStore),
                new TxUpdatedIntervalVariableAccessor(station, stationStore)
        ));
    }
}
