package com.evbox.everon.ocpp.simulator.websocket;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Message consumer is responsible for receiving messages from {@link WebSocketMessageInbox} and forwarding it to the message handler.
 */
@Slf4j
public final class WebSocketMessageConsumer implements Runnable {

    private final WebSocketMessageInbox webSocketMessageInbox;
    private final WebSocketMessageRouter webSocketMessageRouter;

    /**
     * Create message consumer. Should not be instantiated from outside.
     *
     * @param webSocketMessageInbox inbox backed by java.util.concurrent data-structures
     * @param webSocketMessageRouter handles incoming messages
     */
    private WebSocketMessageConsumer(WebSocketMessageInbox webSocketMessageInbox, WebSocketMessageRouter webSocketMessageRouter) {
        this.webSocketMessageInbox = webSocketMessageInbox;
        this.webSocketMessageRouter = webSocketMessageRouter;
    }

    /**
     * Run consumer single-threaded.
     *
     * @param webSocketMessageInbox inbox backed by java.util.concurrent data-structures
     * @param webSocketMessageRouter handles incoming messages
     * @param threadFactory creates threads
     */
    public static void runSingleThreaded(WebSocketMessageInbox webSocketMessageInbox, WebSocketMessageRouter webSocketMessageRouter, ThreadFactory threadFactory) {
        ExecutorService stationCommandsThreadPool = Executors.newSingleThreadExecutor(threadFactory);
        stationCommandsThreadPool.submit(new WebSocketMessageConsumer(webSocketMessageInbox, webSocketMessageRouter));
    }

    /**
     * Task that runs until interrupted.
     */
    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            try {
                AbstractWebSocketClientInboxMessage message = webSocketMessageInbox.take();
                log.debug("RECEIVED MESSAGE:\n{}", message);
                webSocketMessageRouter.route(message);
            } catch (InterruptedException e) {
                log.error("Received interrupt signal to terminate execution", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Could not handle station message", e);
            }

        }

    }
}
