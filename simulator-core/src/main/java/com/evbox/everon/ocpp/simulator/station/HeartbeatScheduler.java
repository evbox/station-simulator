package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.HeartbeatRequest;
import com.evbox.everon.ocpp.v20.message.station.HeartbeatResponse;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FieldDefaults(makeFinal = true)
public class HeartbeatScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatScheduler.class);

    private ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private StationState stationState;
    private StationMessageSender stationMessageSender;

    public HeartbeatScheduler(StationState stationState, StationMessageSender stationMessageSender) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
    }

    public void scheduleHeartbeat(int heartbeatInterval) {
        LOGGER.debug("Scheduling heartbeat to {} sec.", heartbeatInterval);
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, heartbeatInterval, heartbeatInterval, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        try {
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
            Subscriber<HeartbeatRequest, HeartbeatResponse> subscriber = (request, response) -> stationState.setCurrentTime(response.getCurrentTime());

            stationMessageSender.sendHeartBeatAndSubscribe(heartbeatRequest, subscriber);
        } catch (Exception e) {
            LOGGER.error("Unable to send heartbeat", e);
        }
    }
}
