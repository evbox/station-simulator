package com.evbox.everon.ocpp.testutil.station;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import lombok.Data;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Represents an expectation of the response.
 */
@Data
public class ResponseExpectationMatcher {

    private final Predicate<CallResult> expectedResponse;

    private boolean matched = false;

    /**
     * Verify whether response expectation matches or not.
     *
     * @param response outgoing response
     * @return true if matches otherwise false
     */
    public boolean match(CallResult response) {
        if (expectedResponse.test(response)) {
            matched = true;
            return true;
        }
        return false;
    }

    /**
     * Verifies if the expectation was matched
     */
    public void verify() {
        if (!matched) {
            fail("Verification failed, one ore more expected responses not matched");
        }
    }
}
