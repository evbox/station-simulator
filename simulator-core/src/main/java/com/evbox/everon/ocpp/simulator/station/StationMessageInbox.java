package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.exceptions.StationException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Inbox of station messages. It is backed by java.util.concurrent.* queues.
 */
@Slf4j
public class StationMessageInbox {

    private final BlockingQueue<StationMessage> stationMessagesInbox;

    public StationMessageInbox() {
        this.stationMessagesInbox = new LinkedBlockingQueue<>();
    }


    /**
     * Inserts the message into this inbox if it is possible. If no space available throws {@link StationException}.
     *
     * @param message station messages
     */
    public void offer(StationMessage message) {
        boolean success = stationMessagesInbox.offer(message);

        if (!success) {
            throw new StationException("No queue capacity to route command");
        }
    }

    /**
     * Takes the message from the head of this inbox.
     *
     * @return station message
     * @throws InterruptedException if current thread is interrupted.
     */
    public StationMessage take() throws InterruptedException {
        return stationMessagesInbox.take();
    }
}
