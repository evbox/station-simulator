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
    private CableStatus status;

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
     * Change the cable status to {@code CableStatus.PLUGGED}. If status is not {@code CableStatus.UNPLUGGED}
     * then throw {@link IllegalStateException}
     *
     * @return identity of the connector
     */
    public Integer plug() {
        if (status != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s=%s", id, status));
        }
        status = CableStatus.PLUGGED;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.UNPLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unplug() {
        if (status == CableStatus.LOCKED) {
            throw new IllegalStateException(String.format("Connector is locked: %s", id));
        }
        status = CableStatus.UNPLUGGED;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.LOCKED}.
     *
     * @return identity of the connector
     */
    public Integer lock() {
        if (status != CableStatus.PLUGGED) {
            throw new IllegalStateException(String.format("Connector cannot be locked: %s=%s", id, status));
        }
        status = CableStatus.LOCKED;
        return id;
    }

    /**
     * Change the cable status to {@code CableStatus.PLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unlock() {
        status = CableStatus.PLUGGED;
        return id;
    }

    /**
     * Check whether cable status is PLUGGED or not.
     *
     * @return `true` if PLUGGED otherwise `false`
     */
    public boolean isCablePlugged() {
        return status == CableStatus.PLUGGED;
    }

    /**
     * Check whether cable status is LOCKED or not.
     *
     * @return `true` if LOCKED otherwise `false`
     */
    public boolean isCableLocked() {
        return status == CableStatus.LOCKED;
    }

    @Override
    public String toString() {
        return "Connector{" + "id=" + id + ", status=" + status + '}';
    }
}
