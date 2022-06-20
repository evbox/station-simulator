package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v201.message.station.HeartbeatRequest;
import com.evbox.everon.ocpp.v201.message.station.HeartbeatResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Background task that will check if heartbeat should be sent
 */
@Slf4j
public final class HeartbeatSenderTask implements Runnable {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;

    @Getter
    private int heartBeatInterval;
    private LocalDateTime timeOfLastHeartbeatSent;

    public HeartbeatSenderTask(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.stationStore = stationStore;
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
                Subscriber<HeartbeatRequest, HeartbeatResponse> subscriber = (request, response) -> stationStore.setCurrentTime(response.getCurrentTime());
                stationMessageSender.sendHeartBeatAndSubscribe(heartbeatRequest, subscriber);
                timeOfLastHeartbeatSent = now;
            }
        } catch (Exception e) {
            log.error("Heartbeat send exception", e);
        }
    }

    // Send heartbeat if:
    // 1) No message was sent in the previous heartBeatInterval seconds OR
    // 2) No heartbeat was sent in the previous 24 hours
    private boolean shouldSendHeartbeat(LocalDateTime now, LocalDateTime timeOfLastMessageSent) {
        return heartBeatInterval > 0 &&
                (timeOfLastMessageSent.plusSeconds(heartBeatInterval).isBefore(now) || timeOfLastHeartbeatSent.plusDays(1).isBefore(now));
    }
}
