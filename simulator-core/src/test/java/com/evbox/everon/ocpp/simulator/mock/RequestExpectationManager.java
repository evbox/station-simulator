package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.Call;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Manages all request expectations.
 */
public class RequestExpectationManager {

    private final List<String> unexpectedRequests = Collections.synchronizedList(new ArrayList<>());

    private final Set<RequestExpectationMatcher> expectations = Collections.synchronizedSet(new LinkedHashSet<>());

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
    public Optional<Function<Call, String>> findExpectedResponse(Call incomingRequest) {

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

    /**
     * Clear all expectations.
     */
    public void reset() {
        expectations.clear();
        unexpectedRequests.clear();
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
