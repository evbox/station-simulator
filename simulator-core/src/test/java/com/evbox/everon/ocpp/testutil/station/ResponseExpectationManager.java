package com.evbox.everon.ocpp.testutil.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Manages all response expectations
 */
public class ResponseExpectationManager {

    private final List<String> unexpectedResponse = Collections.synchronizedList(new ArrayList<>());

    private final Set<ResponseExpectationMatcher> expectations = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Add unexpected response.
     *
     * @param response unexpected response
     */
    public void addUnexpectedRequest(String response) {
        unexpectedResponse.add(response);
    }

    /**
     * Add new {@link ResponseExpectationMatcher} instance to the manager
     *
     * @param responseExpectationMatcher {@link ResponseExpectationMatcher} instance
     */
    public void add(ResponseExpectationMatcher responseExpectationMatcher) {
        this.expectations.add(responseExpectationMatcher);
    }

    /**
     * Returns true if the expected response is present among expectations
     *
     * @param response outgoing response.
     * @return optional of expected response.
     */
    public boolean isExpectedResponsePresent(CallResult response) {

        List<ResponseExpectationMatcher> successfulMatches =
                expectations.stream().filter(matcher -> matcher.match(response)).collect(toList());

        return !successfulMatches.isEmpty();
    }

    /**
     * Verify all expectations.
     */
    public void verify() {
        for (String unexpectedResponse : unexpectedResponse) {
            fail("No expectations were set. For response: " + unexpectedResponse);
        }

        expectations.forEach(ResponseExpectationMatcher::verify);
    }
}
