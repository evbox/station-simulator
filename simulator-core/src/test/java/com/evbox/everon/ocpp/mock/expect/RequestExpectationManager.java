package com.evbox.everon.ocpp.mock.expect;

import com.evbox.everon.ocpp.mock.match.RequestMatcher;
import com.evbox.everon.ocpp.simulator.message.Call;

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
        return getSuccessfulMatcher(incomingRequest).map(matcher -> Optional.of(matcher.getExpectedResponse())).orElse(Optional.empty());
    }
}
