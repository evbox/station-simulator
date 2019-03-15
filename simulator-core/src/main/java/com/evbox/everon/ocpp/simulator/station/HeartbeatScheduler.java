package com.evbox.everon.ocpp.simulator.station;

import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FieldDefaults(makeFinal = true)
public class HeartbeatScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatScheduler.class);
    private static final int INITIAL_TASK_DELAY_IN_SECONDS = 1;
    private static final int TASK_PERIOD_IN_SECONDS = 1;

    private HeartbeatSenderTask heartbeatTask;

    public HeartbeatScheduler(StationState stationState, StationMessageSender stationMessageSender) {
        this.heartbeatTask = new HeartbeatSenderTask(stationState, stationMessageSender);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                heartbeatTask, INITIAL_TASK_DELAY_IN_SECONDS, TASK_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void updateHeartbeat(int heartbeatInterval) {
        LOGGER.debug("Updating heartbeat to {} sec.", heartbeatInterval);
        heartbeatTask.updateHeartBeatInterval(heartbeatInterval);
    }
}
