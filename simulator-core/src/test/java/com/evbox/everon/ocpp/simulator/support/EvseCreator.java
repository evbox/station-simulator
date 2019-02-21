package com.evbox.everon.ocpp.simulator.support;

import com.evbox.everon.ocpp.simulator.station.evse.*;

import java.util.Collections;

public class EvseCreator {

    public static EvseBuilder createEvse() {
        return new EvseBuilder();
    }

    public static class EvseBuilder {

        private int id;
        private EvseStatus evseStatus;
        private int connectorId;
        private ConnectorStatus connectorStatus;
        private EvseTransaction evseTransaction;

        public EvseBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public EvseBuilder withStatus(EvseStatus evseStatus) {
            this.evseStatus = evseStatus;
            return this;
        }

        public EvseBuilder withConnectorIdAndState(int connectorId, ConnectorStatus connectorStatus) {
            this.connectorId = connectorId;
            this.connectorStatus = connectorStatus;
            return this;
        }

        public EvseBuilder withTransaction(EvseTransaction evseTransaction) {
            this.evseTransaction = evseTransaction;
            return this;
        }

        public Evse build() {
            return new Evse(id, evseStatus, evseTransaction, Collections.singletonList(new Connector(connectorId, connectorStatus)));
        }

    }
}
