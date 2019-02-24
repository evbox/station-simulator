package com.evbox.everon.ocpp.simulator.mock;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Represents an expectation of the request.
 */
@Data
public class RequestExpectationMatcher {

    private final Predicate<String> expectedRequest;
    private final ExpectedCount expectedCount;

    private final String expectedResponse;

    private final AtomicInteger actualCount = new AtomicInteger(0);

    /**
     * Verify whether request expectation matches or not. If matches increment matcher counter.
     *
     * @param incomingRequest incoming request
     * @return true if matches otherwise false
     */
    public boolean match(String incomingRequest) {
        boolean matchResult = expectedRequest.test(incomingRequest);

        if (matchResult) {
            actualCount.incrementAndGet();
        }

        return matchResult;
    }

    /**
     * Return expected response associated with the request.
     *
     * @return expected response.
     */
    public String getExpectedResponse() {
        return expectedResponse;
    }

    /**
     * Verify request expectation.
     */
    public void verify() {
        int actual = actualCount.get();

        if (actual != expectedCount.getCount()) {
            fail("Verification failed. Actual count is " + actual + " but expected " + expectedCount.getCount());
        }
    }

}
