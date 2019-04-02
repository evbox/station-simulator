package com.evbox.everon.ocpp.mock.match;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.mock.expect.ExpectedCount;

import java.util.function.Predicate;

/**
 * Represents an expectation of the response.
 */
public class ResponseMatcher extends Matcher<CallResult> {
    public ResponseMatcher(Predicate<CallResult> expectedRequest, ExpectedCount expectedCount) {
        super(expectedRequest, expectedCount);
    }

}
