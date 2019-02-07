package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.cli.ConsoleReader;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationConfiguration;
import com.evbox.everon.ocpp.simulator.station.StationInboxMessage;
import com.evbox.everon.ocpp.simulator.websocket.LoggingInterceptor;
import com.evbox.everon.ocpp.simulator.websocket.OkHttpWebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClient;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class StationSimulator {

    private final Map<String, Station> stations;
    private final List<WebSocketClient> webSocketClients;

    public StationSimulator(String url, SimulatorConfiguration simulatorConfiguration) {
        this(url, simulatorConfiguration, new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .addNetworkInterceptor(new LoggingInterceptor())
                .pingInterval(10, TimeUnit.SECONDS)
                .build());
    }

    public StationSimulator(String url, SimulatorConfiguration simulatorConfiguration, OkHttpClient client) {
        ImmutableMap.Builder<String, Station> stationMapBuilder = ImmutableMap.builder();
        ImmutableList.Builder<WebSocketClient> webSocketClientsBuilder = ImmutableList.builder();

        simulatorConfiguration.getStations().forEach(configuration -> {
            String stationId = configuration.getId();
            int evseCount = configuration.getEvse().getCount();
            int connectorsPerEvse = configuration.getEvse().getConnectors();

            BlockingQueue<StationInboxMessage> stationInbox = new LinkedBlockingQueue<>();

            ExecutorService stationWorkerExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("station-worker-" + stationId).build());
            ExecutorService messageSenderExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("message-send-worker-" + stationId).build());

            WebSocketClient webSocketClient = new WebSocketClient(messageSenderExecutor, new OkHttpWebSocketClient(url + "/" + stationId, client), stationInbox,
                    WebSocketClientConfiguration.builder().build());

            webSocketClientsBuilder.add(webSocketClient);

            StationConfiguration stationConfiguration = new StationConfiguration(stationId, evseCount, connectorsPerEvse,
                    simulatorConfiguration.getHeartbeatInterval());

            Station station = new Station(stationWorkerExecutor, stationInbox, webSocketClient.getInbox(), stationConfiguration);
            stationMapBuilder.put(stationId, station);
        });

        this.stations = stationMapBuilder.build();
        this.webSocketClients = webSocketClientsBuilder.build();
    }

    public void start() {
        new ConsoleReader(this);
        webSocketClients.forEach(WebSocketClient::start);
        stations.forEach((stationId, station) -> station.start());
    }

    public Station getStation(String stationId) {
        return stations.get(stationId);
    }

    public List<Station> getStations() {
        return new ImmutableList.Builder<Station>().addAll(stations.values()).build();
    }
}
