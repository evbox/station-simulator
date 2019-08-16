package com.evbox.everon.ocpp.simulator;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runner connects and runs stations.
 *
 * <p>This is the entry point for OCPP-Station-Simulator API.</p>
 *
 * <p>{@link StationSimulatorRunner} is not usually reusable. The common scenario is to run, perform actions and digest the results</p>
 */
public class StationSimulatorRunner {

    private final String serverWebSocketUrl;
    private final SimulatorConfiguration simulatorConfiguration;
    private final Map<String, Station> stations;
    public static List<StationSimulatorRunner> instances = new ArrayList();

    /**
     * Create {@link StationSimulatorRunner} with the OCPP server url and configuration.
     *
     * @param serverWebSocketUrl     OCPP server url
     * @param simulatorConfiguration simulator configuration
     */
    public StationSimulatorRunner(String serverWebSocketUrl, SimulatorConfiguration simulatorConfiguration) {
        this.serverWebSocketUrl = serverWebSocketUrl;
        this.simulatorConfiguration = simulatorConfiguration;
        this.stations = new ConcurrentHashMap<>();
        instances.add(this);
    }

    /**
     * Runs OCPP-compliant stations.
     */
    public void run() {
        SimulatorConfiguration.WebSocketConfiguration socketConfiguration = simulatorConfiguration.getSocketConfiguration();
        simulatorConfiguration.getStations().forEach(stationConfiguration -> {

            Station station = new Station(stationConfiguration, socketConfiguration);

            station.connectToServer(serverWebSocketUrl);

            station.run();

            stations.put(stationConfiguration.getId(), station);
        });
    }

    /**
     * Stops OCPP-compliant stations.
     */
    public void stop() {
        stations.values().forEach(Station::stop);

        stations.clear();
    }

    public Station getStation(String stationId) {
        return stations.get(stationId);
    }

    public List<Station> getStations() {
        return new ImmutableList.Builder<Station>().addAll(stations.values()).build();
    }
}
