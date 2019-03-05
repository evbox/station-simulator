package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for {@link ChangeAvailabilityRequest} request.
 */
@Slf4j
public class ChangeAvailabilityRequestHandler implements OcppRequestHandler<ChangeAvailabilityRequest> {

    private final AvailabilityManager availabilityManager;
    private final AvailabilityStateMapper availabilityStateMapper;

    /**
     * Create an instance.
     *
     * @param availabilityManager {@link AvailabilityManager}
     */
    public ChangeAvailabilityRequestHandler(AvailabilityManager availabilityManager) {
        this(availabilityManager, new AvailabilityStateMapper());
    }

    /**
     * Create an instance.
     *
     * @param availabilityManager     {@link AvailabilityManager}
     * @param availabilityStateMapper {@link AvailabilityStateMapper}
     */
    public ChangeAvailabilityRequestHandler(AvailabilityManager availabilityManager, AvailabilityStateMapper availabilityStateMapper) {
        this.availabilityManager = availabilityManager;
        this.availabilityStateMapper = availabilityStateMapper;
    }

    /**
     * Handle {@link ChangeAvailabilityRequest} request.
     * <p>
     * It has 2 scenarios:
     * <p>
     * when evseId == 0 then:
     * change status of all EVSEs
     *
     * <p>
     * when evseId != 0 then
     * change status of the specified EVSE
     *
     * @param callId  identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ChangeAvailabilityRequest request) {

        EvseStatus requestedEvseStatus = availabilityStateMapper.mapFrom(request.getOperationalStatus());

        if (isChangeEvseAvailability(request)) {
            availabilityManager.changeEvseAvailability(callId, request, requestedEvseStatus);
        } else {
            availabilityManager.changeStationAvailability(callId, requestedEvseStatus);
        }

    }

    private boolean isChangeEvseAvailability(ChangeAvailabilityRequest request) {
        return request.getEvseId() != 0;
    }

}
