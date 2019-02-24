package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.Call;
import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Accepts expected response from ocpp server.
 */
@AllArgsConstructor
public class OcppServerResponse {

    private final Predicate<Call> requestExpectation;
    private final ExpectedCount expectedCount;
    private final RequestExpectationManager requestExpectationManager;

    /**
     * Accepts expected response that has to be send when incoming request matches expected request.
     *
     * @param expectedResponse expected response.
     */
    public void thenReturn(Function<Call, String> expectedResponse) {
        requestExpectationManager.add(new RequestExpectationMatcher(requestExpectation, expectedCount, expectedResponse));
    }

}
