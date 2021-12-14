package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.google.common.collect.ImmutableList;

/**
 * Representation of Transaction component according to OCPP 2.0 (3.1.15. TxCtrlr)
 */
public class TxCtrlrComponent extends StationComponent {

    public static final String NAME = "TxCtrlr";

    public TxCtrlrComponent(Station station, StationStore stationStore) {
        super(ImmutableList.of(
                new EVConnectionTimeOutVariableAccessor(station, stationStore),
                new TxStartPointVariableAccessor(station, stationStore),
                new TxStopPointVariableAccessor(station, stationStore),
                new StopTxOnEVSideDisconnectVariableAccessor(station, stationStore),
                new StopTxOnInvalidIdVariableAccessor(station, stationStore)
        ));
    }

    @Override
    public String getComponentName() {
        return NAME;
    }
}
