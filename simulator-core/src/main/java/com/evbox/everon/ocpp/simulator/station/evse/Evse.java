package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionState.*;
import static java.util.Objects.nonNull;

/**
 * An EVSE is considered as an independently operated and managed part of the ChargingStation that can deliver energy to one EV at a time.
 */
@Getter
@EqualsAndHashCode(of = "id")
public class Evse {

    /**
     * Evse identity
     */
    private final int id;

    /**
     * Every Evse may have a list of Connectors
     */
    private final List<Connector> connectors;

    private String tokenId;
    private boolean charging;
    private long seqNo;

    private EvseState evseState;
    private EvseTransaction evseTransaction;
    /**
     * If nonNull should be applied when transaction stops
     */
    private EvseState scheduleNewEvseState;

    /**
     * Create Evse instance. By default evse is in the state AVAILABLE.
     *
     * @param id         evse identity
     * @param connectors list of connectors for this evse
     */
    public Evse(int id, List<Connector> connectors) {
        this(id, EvseState.AVAILABLE, connectors);
    }

    /**
     * Create Evse instance without transaction.
     *
     * @param id         evse identity
     * @param evseState  evse state
     * @param connectors list of connectors for this evse
     */
    public Evse(int id, EvseState evseState, List<Connector> connectors) {
        this(id, evseState, EvseTransaction.NONE, connectors);
    }

    /**
     * Create Evse instance.
     *
     * @param id              evse identity
     * @param evseState       evse state
     * @param evseTransaction evse transaction
     * @param connectors      list of connectors for this evse
     */
    public Evse(int id, EvseState evseState, EvseTransaction evseTransaction, List<Connector> connectors) {
        this.id = id;
        this.evseState = evseState;
        this.evseTransaction = evseTransaction;
        this.connectors = connectors;
    }

    private Evse(int id, List<Connector> connectors, String tokenId, boolean charging, long seqNo, EvseTransaction evseTransaction, EvseState evseState) {
        this.id = id;
        this.connectors = connectors;
        this.tokenId = tokenId;
        this.charging = charging;
        this.seqNo = seqNo;
        this.evseTransaction = evseTransaction;
        this.evseState = evseState;
    }

    /**
     * Copy factory method.
     *
     * @param evse {@link Evse}
     * @return new instance of {@link Evse}
     */
    public static Evse copyOf(Evse evse) {
        List<Connector> connectorsCopy = evse.connectors.stream().map(Connector::copyOf).collect(Collectors.toList());
        return new Evse(evse.id, connectorsCopy, evse.tokenId, evse.charging, evse.seqNo, evse.evseTransaction, evse.evseState);
    }

    /**
     * Find any PLUGGED connector and switch to LOCKED state.
     *
     * @return identity of the connector.
     */
    public Integer lockPluggedConnector() {
        Connector pluggedConnector = connectors.stream()
                .filter(Connector::isPlugged)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to lock connector (nothing is plugged in): evseId=%s", id)));

        pluggedConnector.lock();

        return pluggedConnector.getId();
    }

    /**
     * Find any LOCKED connector and switch to PLUGGED state.
     *
     * @return identity of the connector.
     */
    public Integer unlockConnector() {
        Connector lockedConnector = connectors.stream()
                .filter(Connector::isLocked)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to unlock (no locked connectors): evseId=%s", id)));

        lockedConnector.unlock();

        return lockedConnector.getId();
    }

    /**
     * Find any LOCKED connector and switch to charging state.
     *
     * @return identity of the connector
     */
    public Integer startCharging() {
        Connector lockedConnector = connectors.stream()
                .filter(Connector::isLocked)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Connectors must be in locked state before charging session could be started"));

        charging = true;
        return lockedConnector.getId();
    }

    /**
     * Switch to non-charging state.
     */
    public void stopCharging() {
        charging = false;
    }

    /**
     * Get current sequence number and increment.
     *
     * @return current sequence number
     */
    public Long getSeqNoAndIncrement() {
        return seqNo++;
    }

    /**
     * Setter for tokenId.
     *
     * @param tokenId
     */
    public void setToken(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * Getter for tokenId.
     *
     * @return tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Setter for evse transaction.
     *
     * @param evseTransaction
     */
    public void setEvseTransaction(EvseTransaction evseTransaction) {
        Objects.requireNonNull(evseTransaction);

        this.evseTransaction = evseTransaction;
    }

    /**
     * Setter for evse state.
     *
     * @param evseState
     */
    public void setEvseState(EvseState evseState) {
        this.evseState = evseState;
    }

    /**
     * Setter for scheduled evse state.
     *
     * @param scheduleNewEvseState
     */
    public void setScheduleNewEvseState(EvseState scheduleNewEvseState) {
        this.scheduleNewEvseState = scheduleNewEvseState;
    }

    /**
     * Check whether the given state matches the existing or not.
     *
     * @param requestedEvseState given evse state
     * @return `true` if states do match otherwise `false`
     */
    public boolean hasState(EvseState requestedEvseState) {
        return this.evseState == requestedEvseState;
    }

    /**
     * Check whether transaction is ongoing or not.
     *
     * @return `true` in case if ongoing `false` otherwise
     */
    public boolean hasOngoingTransaction() {
        return evseTransaction.getState() == IN_PROGRESS;
    }

    /**
     * Change evse state if scheduled and stop transaction.
     */
    public void stopTransaction() {
        changeEvseStateIfScheduled();

        evseTransaction.setState(STOPPED);
    }

    /**
     * Clear tokenId.
     */
    public void clearToken() {
        tokenId = StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return "Evse{" +
                "id=" + id +
                ", connectors=" + connectors +
                ", tokenId='" + tokenId + '\'' +
                ", charging=" + charging +
                ", seqNo=" + seqNo +
                ", evseTransaction=" + evseTransaction +
                ", evseState=" + evseState +
                '}';
    }

    private void changeEvseStateIfScheduled() {
        if (nonNull(scheduleNewEvseState)) {
            evseState = scheduleNewEvseState;
            // clean
            scheduleNewEvseState = null;
        }
    }
}
