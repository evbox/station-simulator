package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.handlers.ServerMessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.UserMessageHandler;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.websocket.LoggingInterceptor;
import com.evbox.everon.ocpp.simulator.websocket.OkHttpWebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class is the representation of OCPP station.
 *
 * <p>It should be used to connect to OCPP compliant server for inter-communication.</p>
 *
 * <p>Communication is done via web-socket transport layer using OCPP protocol.</p>
 */
@Slf4j
public class Station {

    private static final OkHttpClient DEFAULT_HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(new LoggingInterceptor())
            .addNetworkInterceptor(new LoggingInterceptor())
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    private final SimulatorConfiguration.StationConfiguration configuration;

    private final StationState state;

    private final WebSocketClient webSocketClient;

    private final HeartbeatScheduler heartbeatScheduler;

    private final SubscriptionRegistry callRegistry;
    private final StationMessageSender stationMessageSender;

    private final StationMessageInbox stationMessageInbox = new StationMessageInbox();

    /**
     * Create a station using {@link SimulatorConfiguration.StationConfiguration} and defaultHeartBeatIntervalSec.
     *
     * @param configuration station configuration
     */
    public Station(SimulatorConfiguration.StationConfiguration configuration) {
        this(configuration, DEFAULT_HTTP_CLIENT);
    }

    /**
     * Create a station using {@link SimulatorConfiguration.StationConfiguration}, defaultHeartBeatIntervalSec and {@link OkHttpClient}.
     *
     * {@link OkHttpClient} is used for connecting and communicating with OCPP server.
     *
     * @param configuration station configuration
     * @param okHttpClient http client
     */
    public Station(SimulatorConfiguration.StationConfiguration configuration, OkHttpClient okHttpClient) {

        this.configuration = configuration;
        this.state = new StationState(configuration);

        this.webSocketClient = new WebSocketClient(stationMessageInbox, configuration.getId(), new OkHttpWebSocketClient(okHttpClient));

        this.callRegistry = new SubscriptionRegistry();
        this.stationMessageSender = new StationMessageSender(callRegistry, state, webSocketClient);
        this.heartbeatScheduler = new HeartbeatScheduler(state, stationMessageSender);
    }

    /**
     * Sends _connect_ request to the OCPP server using given base web-socket url.
     *
     * The connection url has the following structure: serverBaseUrl + stationId
     *
     * @param serverBaseUrl OCPP server base web-socket url
     */
    public void connectToServer(String serverBaseUrl) {

        String stationWebSocketUrl = serverBaseUrl + "/" + configuration.getId();

        webSocketClient.connect(stationWebSocketUrl);

    }

    /**
     * Station starts to receive incoming messages.
     *
     * Sends BootNotification.POWER_UP to the OCPP server.
     *
     */
    public void run() {

        webSocketClient.startAcceptingMessages();

        stationMessageSender.sendBootNotificationAndSubscribe(BootNotificationRequest.Reason.POWER_UP, (request, response) -> {
            if (response.getStatus() == BootNotificationResponse.Status.ACCEPTED) {
                state.setCurrentTime(response.getCurrentTime());

                updateHeartbeat(response.getInterval());

                for (int i = 1; i <= configuration.getEvse().getCount(); i++) {
                    for (int j = 1; j <= configuration.getEvse().getConnectors(); j++) {
                        stationMessageSender.sendStatusNotification(i, j);
                    }
                }
            }
        });

        UserMessageHandler userMessageHandler = new UserMessageHandler(state, stationMessageSender);
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(this, state, stationMessageSender, configuration.getId(), callRegistry);

        StationMessageRouter stationMessageRouter = new StationMessageRouter(serverMessageHandler, userMessageHandler);

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("station-consumer-" + getConfiguration().getId()).build();

        StationMessageConsumer.runSingleThreaded(this, stationMessageInbox, stationMessageRouter, threadFactory);
    }

    /**
     * Sends message to the station directly.
     *
     * @param stationMessage {@link StationMessage}
     */
    public void sendMessage(StationMessage stationMessage) {
        this.stationMessageInbox.offer(stationMessage);
    }

    /**
     * Returns current state of the Station.
     *
     * @return {@link StationState}
     */
    public StationState getStateView() {
        return state;
    }

    /**
     * Returns the station configuration.
     *
     * @return {@link SimulatorConfiguration.StationConfiguration}
     */
    public SimulatorConfiguration.StationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns a map of callId & Call (OCPP-MessageType)
     *
     * @return [callId, {@link Call}] map
     */
    public Map<String, Call> getSentCalls() {
        return stationMessageSender.getSentCalls();
    }

    /**
     * Updates station heartbeat interval
     * @param newHeartbeatInterval heartbeat interval in seconds
     */
    public void updateHeartbeat(int newHeartbeatInterval) {
        heartbeatScheduler.updateHeartbeat(newHeartbeatInterval);
        state.setHeartbeatInterval(newHeartbeatInterval);
    }

    /**
     * Getter for station identity.
     *
     * @return station id
     */
    public String getId() {
        return configuration.getId();
    }
}
