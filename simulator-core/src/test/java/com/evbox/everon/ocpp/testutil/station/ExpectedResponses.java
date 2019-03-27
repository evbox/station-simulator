package com.evbox.everon.ocpp.testutil.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;

import java.util.function.Predicate;

/**
 * Expected station responses
 */
public class ExpectedResponses {

    /**
     * Checks for any response
     *
     * @return check whether the predicate is any response from station
     */
    public static Predicate<CallResult> anyResponse() {
        return request -> true;
    }

    /**
     * Checks for response with a given id
     *
     * @param id response message id
     * @return check whether the predicate has a given message id
     */
    public static Predicate<CallResult> responseWithId(String id) {
        return request -> request.getRawCall().getMessageId().equals(id);
    }
}
