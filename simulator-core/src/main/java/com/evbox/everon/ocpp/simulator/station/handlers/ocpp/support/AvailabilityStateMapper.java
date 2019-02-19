package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support;

import com.evbox.everon.ocpp.simulator.station.evse.EvseState;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseState.AVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseState.UNAVAILABLE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;

/**
 * Mapper from request status to EVSE state.
 */
public class AvailabilityStateMapper {

    private static final Map<OperationalStatus, EvseState> MAPPER = ImmutableMap.of(
            OPERATIVE, AVAILABLE,
            INOPERATIVE, UNAVAILABLE
    );

    /**
     * Map request status to {@link EvseState}.
     *
     * @param operationalStatus {@link OperationalStatus}
     * @return {@link EvseState}
     */
    public static EvseState mapFrom(OperationalStatus operationalStatus) {
        return MAPPER.get(operationalStatus);
    }
}
