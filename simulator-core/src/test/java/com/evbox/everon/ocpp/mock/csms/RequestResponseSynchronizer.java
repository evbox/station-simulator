package com.evbox.everon.ocpp.mock.csms;

import com.evbox.everon.ocpp.simulator.message.CallResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Synchronizer between request send to the station and response received.
 */
@Slf4j
public class RequestResponseSynchronizer {

    // 5 seconds
    private static final int DEFAULT_TIMEOUT = 5_000;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private CallResult stationResponse;

    /**
     * Offers a station response and signals to waiting thread to awaken.
     *
     * @param stationResponse response from the station
     */
    public void offer(CallResult stationResponse) {
        lock.lock();
        try {
            this.stationResponse = stationResponse;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Block until station response is offered. If waiting time is elapsed then throw {@link IllegalStateException}.
     *
     * @return station response
     */
    public CallResult take() {
        lock.lock();
        try {
            while (stationResponse == null) {
                try {
                    boolean isDetectablyElapsed = condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (!isDetectablyElapsed) {
                        throw new IllegalStateException("Waiting time of [" + DEFAULT_TIMEOUT + "] millis had elapsed. No response had been offered.");
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        } finally {
            lock.unlock();
        }

        CallResult response = stationResponse;
        stationResponse = null;
        return response;
    }
}
