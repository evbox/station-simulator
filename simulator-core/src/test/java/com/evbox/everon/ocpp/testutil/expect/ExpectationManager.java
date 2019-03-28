package com.evbox.everon.ocpp.testutil.expect;

import com.evbox.everon.ocpp.testutil.match.Matcher;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class ExpectationManager<S extends Matcher<T>, T> {

    protected final List<String> unexpected = Collections.synchronizedList(new ArrayList<>());

    protected final Set<S> matchers = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Add unexpected call.
     *
     * @param call unexpected call
     */
    public void addUnexpected(String call) {
        unexpected.add(call);
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
        unexpected.clear();
    }

    /**
     * Verify all expectations.
     */
    public void verify() {
        for (String unexpectedResponse : unexpected) {
            fail("No expectations were set. For expectation: " + unexpectedResponse);
        }

        matchers.forEach(Matcher::verify);
    }

    /**
     * Applies all matchers to the given call, and filter the successful ones
     *
     * @param call call to match
     * @return list of successful matchers
     */
    protected List<S> getSuccessfulMatchers(T call) {
        return matchers.stream().filter(matcher -> matcher.match(call)).collect(toList());
    }
}
