package com.evbox.everon.ocpp.testutil.expect;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.testutil.match.ResponseMatcher;

/**
 * Manages all response expectations
 */
public class ResponseExpectationManager extends ExpectationManager<ResponseMatcher, CallResult> {

    /**
     * Returns true if the expected response is present among expectations
     *
     * @param response outgoing response from station
     * @return true if present, false otherwise
     */
    public boolean isExpectedResponsePresent(CallResult response) {
        return !getSuccessfulMatchers(response).isEmpty();
    }

}
