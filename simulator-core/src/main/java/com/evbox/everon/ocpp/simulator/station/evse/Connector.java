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
    private ConnectorState state;

    /**
     * Create a new connector from the specified.
     *
     * @param connector EVSE connector
     * @return a new instance of {@link Connector}
     */
    static Connector copyOf(Connector connector) {
        return new Connector(connector.id, connector.state);
    }

    /**
     * Change the connector state to {@code ConnectorState.PLUGGED}. If state is not {@code ConnectorState.UNPLUGGED}
     * then throw {@link IllegalStateException}
     *
     * @return identity of the connector
     */
    public Integer plug() {
        if (state != ConnectorState.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %s=%s", id, state));
        }
        state = ConnectorState.PLUGGED;
        return id;
    }

    /**
     * Change the connector state to {@code ConnectorState.UNPLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unplug() {
        if (state == ConnectorState.LOCKED) {
            throw new IllegalStateException(String.format("Connector is locked: %s", id));
        }
        state = ConnectorState.UNPLUGGED;
        return id;
    }

    /**
     * Change the connector state to {@code ConnectorState.LOCKED}.
     *
     * @return identity of the connector
     */
    public Integer lock() {
        if (state != ConnectorState.PLUGGED) {
            throw new IllegalStateException(String.format("Connector cannot be locked: %s=%s", id, state));
        }
        state = ConnectorState.LOCKED;
        return id;
    }

    /**
     * Change the connector state to {@code ConnectorState.PLUGGED}.
     *
     * @return identity of the connector
     */
    public Integer unlock() {
        state = ConnectorState.PLUGGED;
        return id;
    }

    /**
     * Check whether state is PLUGGED or not.
     *
     * @return `true` if PLUGGED otherwise `false`
     */
    public boolean isPlugged() {
        return state == ConnectorState.PLUGGED;
    }

    /**
     * Check whether state is LOCKED or not.
     *
     * @return `true` if LOCKED otherwise `false`
     */
    public boolean isLocked() {
        return state == ConnectorState.LOCKED;
    }

    @Override
    public String toString() {
        return "Connector{" + "id=" + id + ", state=" + state + '}';
    }
}
