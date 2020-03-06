package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@FieldDefaults(makeFinal = true)
public class HeartbeatScheduler {

    private static final int INITIAL_TASK_DELAY_IN_SECONDS = 5;
    private static final int TASK_PERIOD_IN_SECONDS = 1;

    private HeartbeatSenderTask heartbeatTask;

    public HeartbeatScheduler(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.heartbeatTask = new HeartbeatSenderTask(stationStore, stationMessageSender);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                heartbeatTask, INITIAL_TASK_DELAY_IN_SECONDS, TASK_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void updateHeartbeat(int heartbeatInterval) {
        log.debug("Updating heartbeat to {} sec.", heartbeatInterval);
        heartbeatTask.updateHeartBeatInterval(heartbeatInterval);
    }
}
