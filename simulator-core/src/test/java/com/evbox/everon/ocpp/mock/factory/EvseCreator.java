package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationRequest;

import java.util.Collections;

public class EvseCreator {

    public static final Evse DEFAULT_EVSE_INSTANCE = createEvse()
            .withId(StationConstants.DEFAULT_EVSE_ID)
            .withStatus(EvseStatus.AVAILABLE)
            .withConnectorId(StationConstants.DEFAULT_CONNECTOR_ID)
            .withCableStatus(CableStatus.UNPLUGGED)
            .withConnectorStatus(ConnectorStatus.AVAILABLE)
            .withTransaction(new EvseTransaction(StationConstants.DEFAULT_TRANSACTION_ID))
            .build();

    public static EvseBuilder createEvse() {
        return new EvseBuilder();
    }

    public static class EvseBuilder {

        private int id;
        private EvseStatus evseStatus;
        private int connectorId;
        private CableStatus cableStatus;
        private EvseTransaction evseTransaction;
        private ConnectorStatus connectorStatus;

        public EvseBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public EvseBuilder withStatus(EvseStatus evseStatus) {
            this.evseStatus = evseStatus;
            return this;
        }

        public EvseBuilder withConnectorId(int connectorId) {
            this.connectorId = connectorId;
            return this;
        }

        public EvseBuilder withCableStatus(CableStatus cableStatus) {
            this.cableStatus = cableStatus;
            return this;
        }

        public EvseBuilder withConnectorStatus(ConnectorStatus connectorStatus) {
            this.connectorStatus = connectorStatus;
            return this;
        }

        public EvseBuilder withTransaction(EvseTransaction evseTransaction) {
            this.evseTransaction = evseTransaction;
            return this;
        }

        public Evse build() {
            return new Evse(id, evseStatus, evseTransaction, Collections.singletonList(new Connector(connectorId, cableStatus, connectorStatus)));
        }
    }
}
