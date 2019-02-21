package com.evbox.everon.ocpp.simulator.station;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Evse {

    public static final String CHARGE_CONTROL_PROTOCOL = "CHAdeMO";

    private static final long MAX_SEQ_NO = Integer.toUnsignedLong(1 + (Integer.MAX_VALUE * 2));

    final Integer id;
    final List<Connector> connectors;
    private String authorizedToken;
    private boolean charging;
    private long seqNo;
    private Integer transactionId;

    static Evse copyOf(Evse evse) {
        List<Connector> connectorsCopy = evse.connectors.stream().map(Connector::copyOf).collect(Collectors.toList());
        return new Evse(evse.id, connectorsCopy, evse.authorizedToken, evse.charging, evse.seqNo, evse.transactionId);
    }

    public enum ConnectorState {
        AVAILABLE, OCCUPIED, LOCKED
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode(of = "id")
    public static class Connector {
        final Integer id;
        ConnectorState state;

        static Connector copyOf(Connector connector) {
            return new Connector(connector.id, connector.state);
        }

        Integer plug() {
            if (state != ConnectorState.AVAILABLE) {
                throw new IllegalStateException(String.format("Connector is not available: %s=%s", id, state));
            }
            state = ConnectorState.OCCUPIED;
            return id;
        }

        Integer unplug() {
            if (state == ConnectorState.LOCKED) {
                throw new IllegalStateException(String.format("Connector is locked: %s", id));
            }
            state = ConnectorState.AVAILABLE;
            return id;
        }

        Integer lock() {
            if (state != ConnectorState.OCCUPIED) {
                throw new IllegalStateException(String.format("Connector cannot be locked: %s=%s", id, state));
            }
            state = ConnectorState.LOCKED;
            return id;
        }

        Integer unlock() {
            state = ConnectorState.OCCUPIED;
            return id;
        }

        boolean isPlugged() {
            return state != ConnectorState.AVAILABLE;
        }

        @Override
        public String toString() {
            return "Connector{" + "id=" + id + ", state=" + state + '}';
        }
    }

    Integer lockPluggedConnector() {
        Connector pluggedConnector = connectors.stream()
                .filter(connector -> connector.getState() == ConnectorState.OCCUPIED)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to lock connector (nothing is plugged in): evseId=%s", id)));

        pluggedConnector.lock();

        return pluggedConnector.getId();
    }

    Integer unlockConnector() {
        Connector lockedConnector = connectors.stream()
                .filter(connector -> connector.getState() == ConnectorState.LOCKED)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to unlock (no locked connectors): evseId=%s", id)));

        lockedConnector.unlock();

        return lockedConnector.getId();
    }

    Integer startCharging() {
        Connector lockedConnector = connectors.stream()
                .filter(connector -> connector.getState() == ConnectorState.LOCKED)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Connectors must be in locked state before charging session could be started"));

        charging = true;
        return lockedConnector.getId();
    }

    void stopCharging() {
        charging = false;
    }

    Long getSeqNo() {
        long currentSeqNo = seqNo;
        if (seqNo == MAX_SEQ_NO) {
            seqNo = 0;
        } else {
            seqNo++;
        }
        return currentSeqNo;
    }


    void storeToken(String tokenId) {
        authorizedToken = tokenId;
    }


    String getTokenId() {
        return authorizedToken;
    }

    void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    void resetToken() {
        authorizedToken = StringUtils.EMPTY;
    }

    boolean hasOngoingTransaction() {
        return transactionId != null;
    }

    void clearTransactionId() {
        this.transactionId = null;
    }

    @Override
    public String toString() {
        return "Evse{" + "id=" + id + ", connectors=" + connectors + ", authorizedToken='" + authorizedToken + '\'' + ", charging=" + charging + ", seqNo=" + seqNo + ", transactionId=" + transactionId + '}';
    }
}
