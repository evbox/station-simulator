package com.evbox.everon.ocpp.simulator.mock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Manages all request expectations.
 */
public class RequestExpectationManager {

    private final List<String> unexpectedRequests = Collections.synchronizedList(new ArrayList<>());

    private final Set<RequestExpectationMatcher> expectations = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Add new {@link RequestExpectationMatcher} instance.
     *
     * @param requestExpectationMatcher {@link RequestExpectationMatcher} instance
     */
    public void add(RequestExpectationMatcher requestExpectationMatcher) {
        this.expectations.add(requestExpectationMatcher);
    }

    /**
     * Find expected response associated with the incoming request. Return {@code Optional.empty()} if nothing was found.
     *
     * @param incomingRequest incoming request.
     * @return optional of expected response.
     */
    public Optional<String> findExpectedResponse(String incomingRequest) {

        for (RequestExpectationMatcher requestExpectationMatcher : expectations) {
            if (requestExpectationMatcher.match(incomingRequest)) {
                return Optional.of(requestExpectationMatcher.getExpectedResponse());
            }
        }

        return Optional.empty();
    }

    /**
     * Add unexpected request.
     *
     * @param incomingRequest unexpected request
     */
    public void addUnexpectedRequest(String incomingRequest) {
        unexpectedRequests.add(incomingRequest);
    }

    /**
     * Verify all expectations.
     */
    public void verify() {

        verifyExpectedRequests();

        verifyUnexpectedRequests();

    }

    private void verifyUnexpectedRequests() {
        if (unexpectedRequests.isEmpty()) return;

        for (String unexpectedRequest : unexpectedRequests) {

            fail("No expectations were set. For incoming request: " + unexpectedRequest);
        }
    }

    private void verifyExpectedRequests() {
        expectations.forEach(RequestExpectationMatcher::verify);
    }
}
