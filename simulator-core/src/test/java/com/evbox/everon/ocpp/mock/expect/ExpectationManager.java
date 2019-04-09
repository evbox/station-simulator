package com.evbox.everon.ocpp.mock.expect;

import com.evbox.everon.ocpp.mock.match.Matcher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class ExpectationManager<S extends Matcher<T>, T> {

    protected final List<String> unexpectedCalls = Collections.synchronizedList(new ArrayList<>());

    protected final Set<S> matchers = Collections.synchronizedSet(new LinkedHashSet<>());

    protected boolean useStrictVerification = false;

    /**
     * Add unexpected call.
     *
     * @param call unexpected call
     */
    public void addUnexpectedCall(String call) {
        unexpectedCalls.add(call);
    }

    /**
     * Add new {@link Matcher} instance.
     *
     * @param matcher {@link Matcher} instance
     */
    public void add(S matcher) {
        this.matchers.add(matcher);
    }

    /**
     * Clear all expectations.
     */
    public void reset() {
        matchers.clear();
        unexpectedCalls.clear();
        useStrictVerification = false;
    }

    /**
     * Use strict verification, and fail if there are any unexpected calls.
     */
    public void useStrictVerification() {
        useStrictVerification = true;
    }

    /**
     * Verify all expectations.
     */
    public void verify() {
        verifyUnexpectedCalls();
        verifyExpectedCalls();
    }

    /**
     * Return the first successful matcher that matches the call
     *
     * @param call call to match
     * @return list of successful matchers
     */
    protected Optional<S> getSuccessfulMatcher(T call) {
        return matchers.stream().filter(matcher -> matcher.match(call)).findFirst();
    }

    private void verifyUnexpectedCalls() {
        if (unexpectedCalls.isEmpty() || !useStrictVerification) return;

        for (String unexpectedCall : unexpectedCalls) {
            fail("No expectations were set. For expectation: " + unexpectedCall);
        }
    }

    private void verifyExpectedCalls() {
        matchers.forEach(Matcher::verify);
    }
}
