package com.evbox.everon.ocpp.testutils.match;

import com.evbox.everon.ocpp.testutils.expect.ExpectedCount;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.fail;

@Data
public abstract class Matcher<T> {
    protected final Predicate<T> expectedRequest;
    protected final ExpectedCount expectedCount;
    protected final AtomicInteger actualCount = new AtomicInteger(0);

    /**
     * Verify whether request expectation matches or not. If matches increment matcher counter.
     *
     * @param request request to be matched
     * @return true if matches otherwise false
     */
    public boolean match(T request) {
        boolean matchResult = expectedRequest.test(request);

        if (matchResult) {
            actualCount.incrementAndGet();
        }

        return matchResult;
    }

    /**
     * Verify request expectation.
     */
    public void verify() {
        int actualCount = this.actualCount.get();
        int expectedCount = this.expectedCount.getCount();
        boolean isExactMatch = this.expectedCount.isExact();

        verifyExact(actualCount, expectedCount, isExactMatch);
        verifyAtLeast(actualCount, expectedCount, isExactMatch);
    }

    private void verifyExact(int actualCount, int expectedCount, boolean isExactMatch) {
        if (isExactMatch && actualCount != expectedCount) {
            fail("Verification failed. Actual count is " + actualCount + " but expected " + expectedCount);
        }
    }

    private void verifyAtLeast(int actualCount, int expectedCount, boolean isExactMatch) {
        if (!isExactMatch && actualCount < expectedCount) {
            fail("Verification failed. Actual count is " + actualCount + " is less than " + expectedCount);
        }
    }
}
