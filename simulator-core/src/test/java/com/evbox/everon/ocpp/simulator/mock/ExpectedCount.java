package com.evbox.everon.ocpp.simulator.mock;

/**
 * Represents an expected count, i.e:
 *
 * once();
 * twice();
 * times(5);
 *
 */
public class ExpectedCount {

    private final int count;

    private ExpectedCount(int count) {
        this.count = count;
    }

    /**
     * Return the expected {@code count} boundary.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * No requests expected.
     */
    public static ExpectedCount never() {
        return new ExpectedCount(0);
    }

    /**
     * Exactly once.
     */
    public static ExpectedCount once() {
        return new ExpectedCount(1);
    }

    /**
     * Exactly twice.
     */
    public static ExpectedCount twice() {
        return new ExpectedCount(2);
    }

    /**
     * Exactly N times.
     */
    public static ExpectedCount times(int count) {
        return new ExpectedCount(count);
    }

}
