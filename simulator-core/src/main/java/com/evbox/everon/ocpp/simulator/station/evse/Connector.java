package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a Connector of the EVSE.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Connector {

    private final Integer id;
    private ConnectorStatus status;

    /**
     * Create a new connector from the specified.
     *
     * @param connector EVSE connector
     * @return a new instance of {@link Connector}
     */
    static Connector copyOf(Connector connector) {
        return new Connector(connector.id, connector.status);
    }

    /**
     * Change the connector status to {@code ConnectorStatus.PLUGGED}. If status is not {@code ConnectorStatus.UNPLUGGED}
     * then throw {@link IllegalStateException}
     *
     * @return identity of the connector
     */
    public Integer plug() {
        if (status != ConnectorStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s=%s", id, status));
        }
        status = ConnectorStatus.PLUGGED;
        return id;
    }

    /**
     * Change the connector status to {@code ConnectorStatus.UNPLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unplug() {
        if (status == ConnectorStatus.LOCKED) {
            throw new IllegalStateException(String.format("Connector is locked: %s", id));
        }
        status = ConnectorStatus.UNPLUGGED;
        return id;
    }

    /**
     * Change the connector status to {@code ConnectorStatus.LOCKED}.
     *
     * @return identity of the connector
     */
    public Integer lock() {
        if (status != ConnectorStatus.PLUGGED) {
            throw new IllegalStateException(String.format("Connector cannot be locked: %s=%s", id, status));
        }
        status = ConnectorStatus.LOCKED;
        return id;
    }

    /**
     * Change the connector status to {@code ConnectorStatus.PLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unlock() {
        status = ConnectorStatus.PLUGGED;
        return id;
    }

    /**
     * Check whether status is PLUGGED or not.
     *
     * @return `true` if PLUGGED otherwise `false`
     */
    public boolean isPlugged() {
        return status == ConnectorStatus.PLUGGED;
    }

    /**
     * Check whether status is LOCKED or not.
     *
     * @return `true` if LOCKED otherwise `false`
     */
    public boolean isLocked() {
        return status == ConnectorStatus.LOCKED;
    }

    @Override
    public String toString() {
        return "Connector{" + "id=" + id + ", status=" + status + '}';
    }
}
