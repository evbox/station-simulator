package com.evbox.everon.ocpp.testutils.expect;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutils.match.RequestMatcher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Manages all request expectations.
 */
public class RequestExpectationManager extends ExpectationManager<RequestMatcher, Call> {

    /**
     * Find expected ocpp response associated with the incoming request.
     * Return {@code Optional.empty()} if nothing was found.
     *
     * @param incomingRequest incoming request.
     * @return optional of expected response.
     */
    public Optional<Function<Call, String>> findExpectedResponse(Call incomingRequest) {

        List<RequestMatcher> matchers = getSuccessfulMatchers(incomingRequest);
        return matchers.isEmpty() ? Optional.empty() : Optional.of(matchers.get(0).getExpectedResponse());
    }
}
