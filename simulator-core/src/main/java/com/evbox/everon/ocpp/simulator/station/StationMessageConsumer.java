package com.evbox.everon.ocpp.simulator.station;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Message consumer is responsible for receiving messages from {@link StationMessageInbox} and forwarding it to the message handler.
 */
@Slf4j
public class StationMessageConsumer implements Runnable {

    private final StationMessageInbox stationMessageInbox;
    private final StationMessageRouter stationMessageRouter;
    private final Station station;

    /**
     * Create message consumer. Should not be instantiated from outside.
     *
     * @param stationMessageInbox inbox backed by java.util.concurrent data-structures
     * @param stationMessageRouter handles incoming messages
     */
    private StationMessageConsumer(Station station, StationMessageInbox stationMessageInbox, StationMessageRouter stationMessageRouter) {
        this.station = station;
        this.stationMessageInbox = stationMessageInbox;
        this.stationMessageRouter = stationMessageRouter;
    }

    /**
     * Run consumer single-threaded.
     *
     * @param stationMessageInbox inbox backed by java.util.concurrent data-structures
     * @param stationMessageRouter handles incoming messages
     * @param threadFactory creates threads
     */
    public static void runSingleThreaded(Station station, StationMessageInbox stationMessageInbox, StationMessageRouter stationMessageRouter, ThreadFactory threadFactory) {
        ExecutorService stationCommandsThreadPool = Executors.newSingleThreadExecutor(threadFactory);
        stationCommandsThreadPool.submit(new StationMessageConsumer(station, stationMessageInbox, stationMessageRouter));
    }

    /**
     * Task that runs until interrupted.
     */
    @Override
    public void run() {

        while(!Thread.currentThread().isInterrupted()) {

            try {
                StationMessage message = stationMessageInbox.take();
                log.debug("RECEIVED MESSAGE:\n{}", message);
                stationMessageRouter.route(message);
            } catch (InterruptedException e) {
                log.error("Received interrupt signal to terminate execution", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Could not handle station message", e);
            }

        }

    }
}
