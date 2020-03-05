package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AlertEventScheduler {

    private static final Duration INITIAL_TASK_DELAY = Duration.ofMinutes(1);
    private static final Duration TASK_PERIOD = Duration.ofMinutes(ThreadLocalRandom.current().nextInt(2, 10));

    private AlertEventSenderTask alertEventSenderTask;

    public AlertEventScheduler(StationMessageSender stationMessageSender) {
        this.alertEventSenderTask = new AlertEventSenderTask(stationMessageSender);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                alertEventSenderTask, INITIAL_TASK_DELAY.toMillis(), TASK_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
    }

}
