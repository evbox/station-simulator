package com.evbox.everon.ocpp.testutil.factory;

import com.evbox.everon.ocpp.simulator.station.evse.*;
import com.evbox.everon.ocpp.testutil.constants.StationConstants;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;

import java.util.Collections;

public class EvseCreator {

    public static final Evse DEFAULT_EVSE_INSTANCE = createEvse()
            .withId(StationConstants.DEFAULT_EVSE_ID)
            .withStatus(EvseStatus.AVAILABLE)
            .withConnectorId(StationConstants.DEFAULT_CONNECTOR_ID)
            .withCableStatus(CableStatus.UNPLUGGED)
            .withConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE)
            .withTransaction(new EvseTransaction(StationConstants.DEFAULT_INT_TRANSACTION_ID))
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
        private StatusNotificationRequest.ConnectorStatus connectorStatus;

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

        public EvseBuilder withConnectorStatus(StatusNotificationRequest.ConnectorStatus connectorStatus) {
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
