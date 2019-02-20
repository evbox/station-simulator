package com.evbox.everon.ocpp.simulator.support;

import com.evbox.everon.ocpp.simulator.station.evse.*;

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
        private EvseTransaction evseTransaction;

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

        public EvseBuilder withTransaction(EvseTransaction evseTransaction) {
            this.evseTransaction = evseTransaction;
            return this;
        }

        public Evse build() {
            return new Evse(id, evseState, evseTransaction, Collections.singletonList(new Connector(connectorId, connectorState)));
        }

    }
}
