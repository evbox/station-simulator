package com.evbox.everon.ocpp.simulator.support;

import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.ConnectorState;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseState;

import java.util.Collections;

public class EvseCreator {

    public static EvseBuilder createEvse() {
        return new EvseBuilder();
    }

    public static class EvseBuilder {

        private int id;
        private EvseState evseState;
        private int connectorId;
        private ConnectorState connectorState;

        public EvseBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public EvseBuilder withState(EvseState evseState) {
            this.evseState = evseState;
            return this;
        }

        public EvseBuilder withConnectorIdAndState(int connectorId, ConnectorState connectorState) {
            this.connectorId = connectorId;
            this.connectorState = connectorState;
            return this;
        }

        public Evse build() {
            return new Evse(id, evseState, Collections.singletonList(new Connector(connectorId, connectorState)));
        }

    }
}
