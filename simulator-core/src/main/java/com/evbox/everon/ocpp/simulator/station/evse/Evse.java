package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.STOPPED;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * An EVSE is considered as an independently operated and managed part of the ChargingStation that can deliver energy to one EV at a time.
 */
@Slf4j
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

    private EvseStatus evseStatus;
    private EvseTransaction transaction;
    /**
     * If nonNull should be applied when transaction stops
     */
    private EvseStatus scheduledNewEvseStatus;

    /**
     * Create Evse instance. By default evse is in the status AVAILABLE.
     *
     * @param id         evse identity
     * @param connectors list of connectors for this evse
     */
    public Evse(int id, List<Connector> connectors) {
        this(id, EvseStatus.AVAILABLE, connectors);
    }

    /**
     * Create Evse instance without transaction.
     *
     * @param id         evse identity
     * @param evseStatus evse status
     * @param connectors list of connectors for this evse
     */
    public Evse(int id, EvseStatus evseStatus, List<Connector> connectors) {
        this(id, evseStatus, EvseTransaction.NONE, connectors);
    }

    /**
     * Create Evse instance.
     *
     * @param id          evse identity
     * @param evseStatus  evse status
     * @param transaction evse transaction
     * @param connectors  list of connectors for this evse
     */
    public Evse(int id, EvseStatus evseStatus, EvseTransaction transaction, List<Connector> connectors) {
        this.id = id;
        this.evseStatus = evseStatus;
        this.transaction = transaction;
        this.connectors = connectors;
    }

    private Evse(int id, List<Connector> connectors, String tokenId, boolean charging, long seqNo, EvseTransaction transaction, EvseStatus evseStatus) {
        this.id = id;
        this.connectors = connectors;
        this.tokenId = tokenId;
        this.charging = charging;
        this.seqNo = seqNo;
        this.transaction = transaction;
        this.evseStatus = evseStatus;
    }

    /**
     * Copy factory method.
     *
     * @param evse {@link Evse}
     * @return new instance of {@link Evse}
     */
    public static Evse copyOf(Evse evse) {
        List<Connector> connectorsCopy = evse.connectors.stream().map(Connector::copyOf).collect(Collectors.toList());
        return new Evse(evse.id, connectorsCopy, evse.tokenId, evse.charging, evse.seqNo, evse.transaction, evse.evseStatus);
    }

    /**
     * Find any LOCKED connector and switch to charging status.
     *
     * @return identity of the connector
     */
    public Integer startCharging() {
        Connector lockedConnector = connectors.stream()
                .filter(Connector::isCableLocked)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Connectors must be in locked status before charging session could be started"));

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
     * @param transaction
     */
    public void setTransaction(EvseTransaction transaction) {
        Objects.requireNonNull(transaction);

        this.transaction = transaction;
    }

    /**
     * Change EVSE status and status of connectors.
     *
     * @param evseStatus
     */
    public void changeStatus(EvseStatus evseStatus) {
        this.evseStatus = evseStatus;
        log.info("Changing status to {} for evse {}", evseStatus, id);
        evseStatus.changeConnectorStatus(connectors);

    }

    /**
     * Setter for scheduled evse status.
     *
     * @param scheduledNewEvseStatus
     */
    public void setScheduledNewEvseStatus(EvseStatus scheduledNewEvseStatus) {
        this.scheduledNewEvseStatus = scheduledNewEvseStatus;
    }

    /**
     * Check whether the given status matches the existing or not.
     *
     * @param requestedEvseStatus given evse status
     * @return `true` if status do match otherwise `false`
     */
    public boolean hasStatus(EvseStatus requestedEvseStatus) {
        return this.evseStatus == requestedEvseStatus;
    }

    /**
     * Check whether transaction is ongoing or not.
     *
     * @return `true` in case if ongoing `false` otherwise
     */
    public boolean hasOngoingTransaction() {
        return transaction.getStatus() == IN_PROGRESS;
    }

    /**
     * Checks whether EVSE has a token or not.
     *
     * @return true if token does exist otherwise false
     */
    public boolean hasTokenId() {
        return isNotBlank(tokenId);
    }

    /**
     * Change evse status if scheduled and stop transaction.
     */
    public void stopTransaction() {
        changeEvseStatusIfScheduled();

        transaction.setStatus(STOPPED);
    }

    /**
     * Create a new transaction with the given id.
     *
     * @param transactionId transaction identity
     */
    public void createTransaction(int transactionId) {
        transaction = new EvseTransaction(transactionId);
    }

    /**
     * Clear tokenId.
     */
    public void clearToken() {
        tokenId = StringUtils.EMPTY;
    }


    /**
     * Plug connector.
     *
     * @param connectorId connector identity
     * @return true if succeeded otherwise false
     */
    public void plug(Integer connectorId) {

        Connector connector = findConnector(connectorId);
        connector.plug();
    }

    /**
     * Unplug connector.
     *
     * @param connectorId connector identity
     * @return true if succeeded otherwise false
     */
    public void unplug(Integer connectorId) {

        Connector connector = findConnector(connectorId);
        connector.unplug();
    }


    /**
     * Find any PLUGGED connector and switch to LOCKED status.
     *
     * @return identity of the connector.
     */
    public Integer lockPluggedConnector() {

        if (evseStatus.isUnavailable()) {
            throw new IllegalStateException("Could not lock plugged connector as EVSE is unavailable");
        }

        Connector pluggedConnector = connectors.stream()
                .filter(Connector::isCablePlugged)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to lock connector (nothing is plugged in): evseId=%s", id)));

        pluggedConnector.lock();

        return pluggedConnector.getId();
    }

    /**
     * Find any LOCKED connector and switch to PLUGGED status.
     *
     * @return identity of the connector.
     */
    public Integer unlockConnector() {

        Connector lockedConnector = connectors.stream()
                .filter(Connector::isCableLocked)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to unlock (no locked connectors): evseId=%s", id)));

        lockedConnector.unlock();

        return lockedConnector.getId();
    }

    /**
     * Check whether cable is plugged to any of EVSE connectors or not.
     *
     * @return true if cable plugged otherwise false
     */
    public boolean isCablePlugged() {
        return getConnectors().stream().anyMatch(Connector::isCablePlugged);
    }

    /**
     * Find an instance of {@link Connector} by connector_id.
     *
     * @param connectorId connector identity
     * @return {@link Connector} instance
     */
    public Connector findConnector(int connectorId) {
        return connectors.stream()
                .filter(connector -> connector.getId().equals(connectorId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No connector with ID: %s", connectorId)));
    }

    @Override
    public String toString() {
        return "Evse{" +
                "id=" + id +
                ", connectors=" + connectors +
                ", tokenId='" + tokenId + '\'' +
                ", charging=" + charging +
                ", seqNo=" + seqNo +
                ", transaction=" + transaction +
                ", evseStatus=" + evseStatus +
                '}';
    }

    private void changeEvseStatusIfScheduled() {
        if (nonNull(scheduledNewEvseStatus)) {
            changeStatus(scheduledNewEvseStatus);
            // clean
            scheduledNewEvseStatus = null;
        }
    }

}
