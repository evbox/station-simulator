package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support;

import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v201.message.station.OperationalStatus;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.AVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.v201.message.station.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v201.message.station.OperationalStatus.OPERATIVE;
import static java.util.Objects.nonNull;

/**
 * Mapper from {@link OperationalStatus} status to EVSE status.
 */
public class AvailabilityStateMapper {

    private static final Map<OperationalStatus, EvseStatus> MAPPER = ImmutableMap.of(
            OPERATIVE, AVAILABLE,
            INOPERATIVE, UNAVAILABLE
    );

    /**
     * Map request status to {@link EvseStatus}.
     *
     * @param operationalStatus {@link OperationalStatus}
     * @return {@link EvseStatus}
     */
    public EvseStatus mapFrom(OperationalStatus operationalStatus) {
        EvseStatus evseStatus = MAPPER.get(operationalStatus);

        if (nonNull(evseStatus)) {
            return evseStatus;
        }

        throw new IllegalArgumentException(String.format("Could not find appropriate evse status for operational status %s", operationalStatus));
    }
}
