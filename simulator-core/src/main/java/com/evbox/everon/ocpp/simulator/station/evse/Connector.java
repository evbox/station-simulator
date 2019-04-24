package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.ThreadSafe;

import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.OCCUPIED;

/**
 * Represents a Connector of the EVSE.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ThreadSafe
public class Connector {

    private final Integer id;
    private volatile CableStatus cableStatus;
    private volatile ConnectorStatus connectorStatus;

    /**
     * Change the cable status to {@code CableStatus.PLUGGED}. If status is not {@code CableStatus.UNPLUGGED}
     * then throw {@link IllegalStateException}
     *
     * @return identity of the connector
     */
    public synchronized Integer plug() {
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
    public synchronized Integer unplug() {
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
    public synchronized Integer lock() {
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
