package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.evbox.everon.ocpp.v201.message.station.ConnectorStatus;

import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.OCCUPIED;

/**
 * Represents a Connector of the EVSE.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Connector {

    private final Integer id;
    private CableStatus cableStatus;
    private ConnectorStatus connectorStatus;

    /**
     * Change the cable status to {@code CableStatus.PLUGGED}. If status is not {@code CableStatus.UNPLUGGED}
     * then throw {@link IllegalStateException}
     *
     * @return identity of the connector
     */
    public Integer plug() {
        if (cableStatus != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s=%s", id, cableStatus));
        }
        cableStatus = CableStatus.PLUGGED;

        connectorStatus = OCCUPIED;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.UNPLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unplug() {
        if (cableStatus == CableStatus.LOCKED) {
            throw new IllegalStateException(String.format("Connector is locked: %s", id));
        }
        cableStatus = CableStatus.UNPLUGGED;

        connectorStatus = AVAILABLE;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.LOCKED}.
     *
     * @return identity of the connector
     */
    public Integer lock() {
        if (cableStatus != CableStatus.PLUGGED) {
            throw new IllegalStateException(String.format("Connector cannot be locked: %s=%s", id, cableStatus));
        }
        cableStatus = CableStatus.LOCKED;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.PLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unlock() {
        cableStatus = CableStatus.PLUGGED;
        return id;
    }

    /**
     * Setter for connector status.
     *
     * @param connectorStatus {@link ConnectorStatus}
     */
    public void setConnectorStatus(ConnectorStatus connectorStatus) {
        this.connectorStatus = connectorStatus;
    }

    /**
     * Check whether cable status is PLUGGED or not.
     *
     * @return `true` if PLUGGED otherwise `false`
     */
    public boolean isCablePlugged() {
        return cableStatus == CableStatus.PLUGGED;
    }

    /**
     * Check whether cable status is LOCKED or not.
     *
     * @return `true` if LOCKED otherwise `false`
     */
    public boolean isCableLocked() {
        return cableStatus == CableStatus.LOCKED;
    }


    @Override
    public String toString() {
        return "Connector{" +
                "id=" + id +
                ", cableStatus=" + cableStatus +
                ", connectorStatus=" + connectorStatus +
                '}';
    }

    ConnectorView createView() {
        return new ConnectorView(this.id, this.cableStatus, this.connectorStatus);
    }

    @Getter
    @AllArgsConstructor
    public class ConnectorView {

        private final Integer id;
        private final CableStatus cableStatus;
        private final ConnectorStatus connectorStatus;
    }
}
