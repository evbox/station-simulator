package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PeriodicEventScheduler {

    private static final Duration INITIAL_TASK_DELAY = Duration.ofMinutes(10);
    private static final Duration TASK_PERIOD = Duration.ofHours(4);

    private PeriodicEventSenderTask periodicEventSenderTask;

    public PeriodicEventScheduler(StationMessageSender stationMessageSender) {
        this.periodicEventSenderTask = new PeriodicEventSenderTask(stationMessageSender);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                periodicEventSenderTask, INITIAL_TASK_DELAY.toMillis(), TASK_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
    }

}
