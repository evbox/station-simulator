package com.evbox.everon.ocpp.simulator.configuration;

import lombok.Data;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
public class SimulatorConfiguration {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    /**
     * Default heartbeatInterval for all stations
     */
    private Integer heartbeatInterval;
    private List<StationConfiguration> stations;

    public int getHeartbeatInterval() {
        return defaultIfNull(heartbeatInterval, DEFAULT_HEARTBEAT_INTERVAL);
    }

    @Data
    public static class StationConfiguration {
        private String id;
        private Evse evse;

    }

    @Data
    public static class Evse {
        /**
         * Amount of EVSEs for current station
         */
        private int count;

        /**
         * Amount of connectors per each EVSE
         */
        private int connectors;
    }
}