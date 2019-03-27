package com.evbox.everon.ocpp.testutil.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;

import java.util.function.Predicate;

/**
 * Expected response from station
 */
public class StationExpectedResponse {

    private final ResponseExpectationManager responseExpectationManager;

    public StationExpectedResponse(ResponseExpectationManager responseExpectationManager, Predicate<CallResult> responseExpectation) {
        this.responseExpectationManager = responseExpectationManager;
        responseExpectationManager.add(new ResponseExpectationMatcher(responseExpectation));
    }

    /**
     * Accepts expected response that has to be send when incoming request matches expected request.
     *
     * @param responseExpectation expected response.
     */
    public StationExpectedResponse expectResponseFromStation(Predicate<CallResult> responseExpectation) {
        return new StationExpectedResponse(responseExpectationManager, responseExpectation);
    }
}
