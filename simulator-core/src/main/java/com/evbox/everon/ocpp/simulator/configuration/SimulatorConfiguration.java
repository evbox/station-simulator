package com.evbox.everon.ocpp.simulator.configuration;

import lombok.Data;

import java.util.List;

@Data
public class SimulatorConfiguration {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private List<StationConfiguration> stations;

    @Data
    public static class StationConfiguration {
        private String id;
        private Evse evse;
        private String password;
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