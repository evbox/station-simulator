package com.evbox.everon.ocpp.mock.expect;

/**
 * Represents an expected count, i.e:
 * <p>
 * once();
 * twice();
 * times(5);
 */
public class ExpectedCount {
    private static final boolean EXACT = true;

    private final int count;
    private final boolean exact;

    private ExpectedCount(int count, boolean exact) {
        this.count = count;
        this.exact = exact;
    }

    /**
     * Return the expected {@code count} boundary.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Returns true if the count is exact.
     */
    public boolean isExact() { return this.exact; }

    /**
     * No requests expected.
     */
    public static ExpectedCount never() { return new ExpectedCount(0, EXACT); }

    /**
     * Exactly once.
     */
    public static ExpectedCount once() { return new ExpectedCount(1, EXACT); }

    /**
     * Exactly N times.
     */
    public static ExpectedCount times(int count) { return new ExpectedCount(count, EXACT); }

    /**
     * At least once.
     */
    public static ExpectedCount atLeastOnce() { return new ExpectedCount(1, !EXACT); }

    /**
     * At least N times.
     */
    public static ExpectedCount atLeast(int count) { return new ExpectedCount(count, !EXACT); }

    /**
     * Any amount of times.
     */
    public static ExpectedCount any() { return new ExpectedCount(0, !EXACT); }
}
