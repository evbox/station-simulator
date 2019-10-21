package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.station.exceptions.StationException;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class WebSocketMessageInbox {

    private final BlockingQueue<AbstractWebSocketClientInboxMessage> webSocketMessageInboxQueue;

    public WebSocketMessageInbox() {
        this.webSocketMessageInboxQueue = new PriorityBlockingQueue<>(1,
                Comparator.comparing(AbstractWebSocketClientInboxMessage::getPriority).thenComparing(AbstractWebSocketClientInboxMessage::getSequenceId));
    }

    /**
     * Add message into inbox. If no space available throws {@link StationException}.
     *
     * @param message station messages
     */
    public void offer(AbstractWebSocketClientInboxMessage message) {
        boolean success = webSocketMessageInboxQueue.offer(message);

        if (!success) {
            throw new StationException("Failed on adding message to the inbox");
        }
    }

    /**
     * Take message from the head of the inbox.
     *
     * @return station message
     * @throws InterruptedException if current thread is interrupted.
     */
    public AbstractWebSocketClientInboxMessage take() throws InterruptedException {
        return webSocketMessageInboxQueue.take();
    }

}
