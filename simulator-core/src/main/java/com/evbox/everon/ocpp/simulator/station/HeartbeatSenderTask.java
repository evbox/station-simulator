package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.HeartbeatRequest;
import com.evbox.everon.ocpp.v20.message.station.HeartbeatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Background task that will check if heartbeat should be sent
 */
public final class HeartbeatSenderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatSenderTask.class);

    private int heartBeatInterval;
    private StationState stationState;
    private StationMessageSender stationMessageSender;
    private LocalDateTime timeOfLastHeartbeatSent;

    public HeartbeatSenderTask(StationState stationState, StationMessageSender stationMessageSender) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
        this.heartBeatInterval = 0;
        this.timeOfLastHeartbeatSent = LocalDateTime.MIN;
    }

    public void updateHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    @Override
    public void run() {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (shouldSendHeartbeat(now, stationMessageSender.getTimeOfLastMessageSent())) {
                HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
                Subscriber<HeartbeatRequest, HeartbeatResponse> subscriber = (request, response) -> stationState.setCurrentTime(response.getCurrentTime());
                stationMessageSender.sendHeartBeatAndSubscribe(heartbeatRequest, subscriber);
                timeOfLastHeartbeatSent = now;
            }
        } catch (Exception e) {
            LOGGER.error("Heartbeat send exception", e);
        }
    }

    private boolean shouldSendHeartbeat(LocalDateTime now, LocalDateTime timeOfLastMessageSent) {
        return heartBeatInterval > 0 &&
                (ChronoUnit.SECONDS.between(timeOfLastMessageSent, now) > heartBeatInterval ||
                ChronoUnit.DAYS.between(timeOfLastHeartbeatSent, now) > 1);

    }
}
