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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@FieldDefaults(makeFinal = true)
public class HeartbeatScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatScheduler.class);

    private ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private StationState stationState;
    private StationMessageSender stationMessageSender;

    private AtomicInteger heartBeatInterval;
    private AtomicLong timeOfLastHeartbeatSent;

    public HeartbeatScheduler(StationState stationState, StationMessageSender stationMessageSender) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
        this.heartBeatInterval = new AtomicInteger();
        this.timeOfLastHeartbeatSent = new AtomicLong(0);
    }

    public void updateHeartbeat(int heartbeatInterval) {
        LOGGER.debug("Scheduling heartbeat to {} sec.", heartbeatInterval);
        heartbeatExecutor.scheduleAtFixedRate(this::shouldSendHeartbeat, heartbeatInterval, heartbeatInterval, TimeUnit.SECONDS);
    }

    private void shouldSendHeartbeat() {
        long now = System.currentTimeMillis();
        long lastHeartbeat = timeOfLastHeartbeatSent.get();
        long lastMessage = stationMessageSender.getTimeOfLastMessageSent();
        long HeartbeatIntervalInMs = 1000L * heartBeatInterval.get();

        if (now - lastMessage > HeartbeatIntervalInMs || now - lastHeartbeat > TimeUnit.DAYS.toMillis(1)) {
            sendHeartbeat();
        }
    }

    private void sendHeartbeat() {
        try {
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
            Subscriber<HeartbeatRequest, HeartbeatResponse> subscriber = (request, response) -> stationState.setCurrentTime(response.getCurrentTime());
            stationMessageSender.sendHeartBeatAndSubscribe(heartbeatRequest, subscriber);
            timeOfLastHeartbeatSent.set(System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.error("Unable to send heartbeat", e);
        }
    }
}
