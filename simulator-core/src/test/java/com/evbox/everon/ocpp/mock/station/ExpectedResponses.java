package com.evbox.everon.ocpp.mock.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;

import java.util.function.Predicate;

/**
 * Expected station responses
 */
public class ExpectedResponses {

    /**
     * Checks for response with a given id
     *
     * @param id response message id
     * @return predicate that checks whether the CallResult has a specified id
     */
    public static Predicate<CallResult> responseWithId(String id) {
        return request -> request.getRawCall().getMessageId().equals(id);
    }
}
