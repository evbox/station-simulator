package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support;

import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChangeAvailabilityFuture {

    private final ExecutorService threadPool;

    /**
     * Create an instance with the threadPool sized to what {@code Runtime.getRuntime().availableProcessors()} return
     */
    public ChangeAvailabilityFuture() {
        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Create an instance with the given threadPool.
     *
     * @param threadPool
     */
    public ChangeAvailabilityFuture(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Tries to set a new state withing a given time frame. It sets the new state if evse has no ongoing transaction.
     *
     * @param evse                         {@link Evse}
     * @param newState                     a new evse state
     * @param attempts                     number of attempts to set a new state
     * @param delayBetweenAttemptsInMillis delay between attempts in milliseconds
     */
    public void runAsync(Evse evse, EvseState newState, int attempts, long delayBetweenAttemptsInMillis) {

        CompletableFuture.runAsync(() -> {
            int count = 0;

            while (count++ < attempts) {

                if (!evse.hasOngoingTransaction()) {
                    evse.setEvseState(newState);
                    return;
                }

                park(delayBetweenAttemptsInMillis);
            }

            log.warn("Could not change state of the EVSE with id {}. All attempts were used.", evse.getId());
        }, threadPool);

    }

    private void park(long delayBetweenAttemptsInMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(delayBetweenAttemptsInMillis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
