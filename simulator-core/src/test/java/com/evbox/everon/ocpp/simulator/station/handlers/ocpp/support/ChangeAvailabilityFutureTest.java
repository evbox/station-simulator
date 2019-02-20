package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support;

import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseState;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseState.AVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseState.UNAVAILABLE;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionState.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionState.NONE;
import static com.evbox.everon.ocpp.simulator.support.EvseCreator.createEvse;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_SECOND;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ChangeAvailabilityFutureTest {

    private static final int ATTEMPTS = 5;
    private static final int DELAY_BETWEEN_ATTEMPTS = 100;

    ThreadPoolExecutor executorService = newSingleThreadExecutor();

    ChangeAvailabilityFuture changeAvailabilityFuture = new ChangeAvailabilityFuture(executorService);

    @Test
    void shouldChangeStateImmediately() {

        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.UNAVAILABLE)
                .withTransaction(new EvseTransaction(Integer.valueOf(DEFAULT_TRANSACTION_ID), NONE))
                .build();

        changeAvailabilityFuture.runAsync(evse, AVAILABLE, ATTEMPTS, DELAY_BETWEEN_ATTEMPTS);

        await().atMost(ONE_SECOND).untilAsserted(() -> assertThat(evse.getEvseState()).isEqualTo(AVAILABLE));

    }

    @Test
    void shouldChangeStateAfterTransactionHasCompleted() {

        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.UNAVAILABLE)
                .withTransaction(new EvseTransaction(Integer.valueOf(DEFAULT_TRANSACTION_ID), IN_PROGRESS))
                .build();

        changeAvailabilityFuture.runAsync(evse, AVAILABLE, ATTEMPTS, DELAY_BETWEEN_ATTEMPTS);

        int millis = 300;

        parkAndRun(millis, evse::stopTransaction);

        await().atMost(TWO_SECONDS).untilAsserted(() -> {

            assertAll(
                    () -> assertThat(executorService.getActiveCount()).isEqualTo(0),
                    () -> assertThat(evse.getEvseState()).isEqualTo(AVAILABLE)
            );

        });

    }

    @Test
    @DisplayName("Should not change state if number of attempts has exceeded the threshold")
    void shouldNotChangeState() {

        Evse evse = createEvse()
                .withId(DEFAULT_EVSE_ID)
                .withState(EvseState.UNAVAILABLE)
                .withTransaction(new EvseTransaction(Integer.valueOf(DEFAULT_TRANSACTION_ID), IN_PROGRESS))
                .build();

        int attempts = 20;

        changeAvailabilityFuture.runAsync(evse, AVAILABLE, attempts, DELAY_BETWEEN_ATTEMPTS);

        await().pollDelay(ONE_SECOND).untilAsserted(() -> {

            assertAll(
                    () -> assertThat(executorService.getActiveCount()).isEqualTo(1),
                    () -> assertThat(evse.getEvseState()).isEqualTo(UNAVAILABLE)
            );

        });

    }

    void parkAndRun(long millis, Runnable task) {

        CompletableFuture.runAsync(() -> {

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(millis));

            task.run();
        });

    }

    ThreadPoolExecutor newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

}
