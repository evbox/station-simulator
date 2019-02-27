package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;

import java.util.List;

/**
 * Status of station EVSE.
 *
 * AVAILABLE corresponds to Operative
 * UNAVAILABLE corresponds to Inoperative
 */
public enum EvseStatus {

    AVAILABLE {
        @Override
        public void changeConnectorStatus(List<Connector> connectors) {
            connectors.forEach(connector -> connector.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE));
        }
    },
    UNAVAILABLE {
        @Override
        public void changeConnectorStatus(List<Connector> connectors) {
            connectors.forEach(connector -> connector.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.UNAVAILABLE));
        }
    };

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isUnAvailable() {
        return this == UNAVAILABLE;
    }

    public abstract void changeConnectorStatus(List<Connector> connectors);

}