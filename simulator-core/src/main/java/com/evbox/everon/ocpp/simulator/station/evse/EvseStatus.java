package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;
import com.evbox.everon.ocpp.v201.message.station.StatusNotificationRequest;

import java.util.List;

/**
 * Status of station EVSE.
 *
 * AVAILABLE corresponds to Operative
 * UNAVAILABLE corresponds to Inoperative
 */
public enum EvseStatus {

    AVAILABLE("Available") {
        @Override
        public void changeConnectorStatus(List<Connector> connectors) {
            connectors.forEach(connector -> connector.setConnectorStatus(ConnectorStatus.AVAILABLE));
        }
    },
    UNAVAILABLE("Unavailable") {
        @Override
        public void changeConnectorStatus(List<Connector> connectors) {
            connectors.forEach(connector -> connector.setConnectorStatus(ConnectorStatus.UNAVAILABLE));
        }
    };
    private final String value;

    EvseStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isUnavailable() {
        return this == UNAVAILABLE;
    }

    public abstract void changeConnectorStatus(List<Connector> connectors);

}
