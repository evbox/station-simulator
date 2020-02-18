package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.StationStore.StationStoreView;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.handlers.ServerMessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.SystemMessageHandler;
import com.evbox.everon.ocpp.simulator.station.handlers.UserMessageHandler;
import com.evbox.everon.ocpp.simulator.station.subscription.SubscriptionRegistry;
import com.evbox.everon.ocpp.simulator.station.support.SecurityUtils;
import com.evbox.everon.ocpp.simulator.websocket.LoggingInterceptor;
import com.evbox.everon.ocpp.simulator.websocket.OkHttpWebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

/**
 * This class is the representation of OCPP station.
 *
 * <p>It should be used to connect to OCPP compliant server for inter-communication.</p>
 *
 * <p>Communication is done via web-socket transport layer using OCPP protocol.</p>
 */
@Slf4j
public class Station {

    private final OkHttpClient defaultHttpClient;
    private final OkHttpClient.Builder defaultHttpClientBuilder;
    private final OkHttpWebSocketClient okHttpWebSocketClient;

    private final SimulatorConfiguration.StationConfiguration configuration;

    private final StationStore state;
    private volatile StationStoreView stationStoreView; // NOSONAR

    private final WebSocketClient webSocketClient;

    private final HeartbeatScheduler heartbeatScheduler;

    private final SubscriptionRegistry callRegistry;
    private final StationMessageSender stationMessageSender;
    private final StateManager stateManager;

    private final StationMessageInbox stationMessageInbox = new StationMessageInbox();

    /**
     * Create a station using {@link SimulatorConfiguration.StationConfiguration} and defaultHeartBeatIntervalSec.
     *
     * @param stationConfiguration station configuration
     */
    public Station(SimulatorConfiguration.StationConfiguration stationConfiguration) {
        this(stationConfiguration, SimulatorConfiguration.WebSocketConfiguration.builder().build());
    }

    /**
     * Create a station using {@link SimulatorConfiguration.StationConfiguration}, {@link SimulatorConfiguration.WebSocketConfiguration} and defaultHeartBeatIntervalSec.
     *
     * @param stationConfiguration station configuration
     * @param socketConfiguration socket configuration
     */
    public Station(SimulatorConfiguration.StationConfiguration stationConfiguration, SimulatorConfiguration.WebSocketConfiguration socketConfiguration) {
        defaultHttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .addNetworkInterceptor(new LoggingInterceptor());

        if (socketConfiguration == null) {
            socketConfiguration = SimulatorConfiguration.WebSocketConfiguration.builder().build();
        }

        defaultHttpClientBuilder.callTimeout(socketConfiguration.getCallTimeoutMs(), TimeUnit.MILLISECONDS);
        defaultHttpClientBuilder.connectTimeout(socketConfiguration.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
        defaultHttpClientBuilder.readTimeout(socketConfiguration.getReadTimeoutMs(), TimeUnit.MILLISECONDS);
        defaultHttpClientBuilder.writeTimeout(socketConfiguration.getWriteTimeoutMs(), TimeUnit.MILLISECONDS);

        defaultHttpClientBuilder.pingInterval(socketConfiguration.getPingIntervalMs(), TimeUnit.MILLISECONDS);

        defaultHttpClient = defaultHttpClientBuilder.build();

        this.configuration = stationConfiguration;
        this.state = new StationStore(configuration);

        this.okHttpWebSocketClient = new OkHttpWebSocketClient(defaultHttpClient, configuration);
        this.webSocketClient = new WebSocketClient(stationMessageInbox, configuration.getId(), okHttpWebSocketClient);

        this.callRegistry = new SubscriptionRegistry();
        this.stationMessageSender = new StationMessageSender(callRegistry, state, webSocketClient);
        this.heartbeatScheduler = new HeartbeatScheduler(state, stationMessageSender);

        SimulatorConfiguration.MeterValuesConfiguration meterValuesConfiguration = stationConfiguration.getMeterValuesConfiguration();
        if (meterValuesConfiguration == null) {
            meterValuesConfiguration = SimulatorConfiguration.MeterValuesConfiguration.builder().build();
        }

        new MeterValuesScheduler(state, stationMessageSender, meterValuesConfiguration.getSendMeterValuesIntervalSec(), meterValuesConfiguration.getConsumptionWattHour());

        this.stateManager = new StateManager(this, state, stationMessageSender);
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

        UserMessageHandler userMessageHandler = new UserMessageHandler(stateManager);
        SystemMessageHandler systemMessageHandler = new SystemMessageHandler(state, stationMessageSender, stateManager);
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(this, state, stationMessageSender, stateManager, configuration.getId(), callRegistry);

        StationMessageRouter stationMessageRouter = new StationMessageRouter(serverMessageHandler, userMessageHandler, systemMessageHandler);

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("station-consumer-" + getConfiguration().getId()).build();

        StationMessageConsumer.runSingleThreaded(this, stationMessageInbox, stationMessageRouter, threadFactory);
    }

    /**
     * Stops simulating a station
     */
    public void stop() {
        webSocketClient.disconnect();
        defaultHttpClient.dispatcher().executorService().shutdownNow();
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
     * @return {@link StationStore}
     */
    public StationStoreView getStateView() {
        if (stationStoreView == null) {
            refreshStateView();
        }
        return stationStoreView;
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
     * Updates station heartbeat interval
     * @param newHeartbeatInterval heartbeat interval in seconds
     */
    public void updateHeartbeat(int newHeartbeatInterval) {
        heartbeatScheduler.updateHeartbeat(newHeartbeatInterval);
        state.setHeartbeatInterval(newHeartbeatInterval);
    }

    /**
     * Updates station connection timeout interval
     * @param newEVConnectionTimeOut ev connection timeout interval in seconds
     */
    public void updateEVConnectionTimeOut(int newEVConnectionTimeOut) {
        state.setEVConnectionTimeOut(newEVConnectionTimeOut);
    }

    /**
     * Updates station txStartPoint values
     * @param txStartPointValues txStartPoint values to apply
     */
    public void updateTxStartPointValues(OptionList<TxStartStopPointVariableValues> txStartPointValues) {
        state.setTxStartPointValues(txStartPointValues);
    }

    /**
     * Updates station txStopPoint values
     * @param txStopPointValues txStopPoint values to apply
     */
    public void updateTxStopPointValues(OptionList<TxStartStopPointVariableValues> txStopPointValues) {
        state.setTxStopPointValues(txStopPointValues);
    }

    /**
     * Updates station network configuration priority values
     * @param networkConfigurationPriorityValues NetworkConfigurationPriority values to apply
     */
    public void updateNetworkConfigurationPriorityValues(Integer networkConfigurationPriorityValues) {
        state.addNetworkConfigurationPriority(networkConfigurationPriorityValues);
    }

    /**
     * Getter for station identity.
     *
     * @return station id
     */
    public String getId() {
        return configuration.getId();
    }

    /**
     * Reconnect station to the OCPP server, instantly.
     */
    public void reconnect() {
        webSocketClient.reconnect();
    }

    /**
     * Reconnect station to the OCCP server, at most before timeout expires.
     * @param timeout timeout before reconnecting
     * @param unit unit of the timeout
     */
    public void reconnect(long timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        reconnect();
    }

    /**
     * Refresh state view.
     */
    void refreshStateView() {
        this.stationStoreView = state.createView();
    }

    /**
     * Stops the current connection and establish a new one using
     * security profile 3.
     *
     * @param url endpoint supporting security profile 3
     */
    public void switchToSecurityProfile3(String url) {
        if (isNull(state.getStationCertificate()) || isNull(state.getStationPublicKey()) || isNull(state.getStationPrivateKey())) {
            throw new IllegalArgumentException("No station certificate found. Please request one!");
        }

        try {
            KeyStore trustStore = SecurityUtils.generateKeyStore(state.getStationCertificate(), state.getStationCertificateChain(), state.getStationPublicKey(), state.getStationPrivateKey());
            X509TrustManager trustManager = SecurityUtils.createTrustManager(trustStore);
            SSLContext sslContext = SecurityUtils.prepareSSLContext(trustStore);

            this.defaultHttpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
            this.okHttpWebSocketClient.setClient(this.defaultHttpClientBuilder.build());

            connectToServer(url);
        } catch (Exception e) {
            log.info("Exception while trying to switch to a profile3 connection", e);
            throw new IllegalStateException(e);
        }
    }

}
