package com.evbox.everon.ocpp.simulator.station.evse;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.IN_PROGRESS;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.STOPPED;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * An EVSE is considered as an independently operated and managed part of the ChargingStation that can deliver energy to one EV at a time.
 */
@Slf4j
@EqualsAndHashCode(of = "id")
@ThreadSafe
public class Evse {

    /**
     * Evse identity
     */
    private final int id;

    /**
     * Every Evse may have a list of Connectors
     */
    private final List<Connector> connectors;

    private volatile boolean charging;
    private volatile String tokenId;
    private final AtomicLong seqNo = new AtomicLong();

    private volatile EvseStatus evseStatus;
    private volatile EvseTransaction transaction;
    /**
     * If nonNull should be applied when transaction stops
     */
    @GuardedBy("this")
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

    /**
     * Getter for id.
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for charging.
     *
     * @return charging
     */
    public boolean isCharging() {
        return charging;
    }

    /**
     * Find any LOCKED connector and switch to charging status.
     *
     * @return identity of the connector
     */
    public void startCharging() {
        charging = true;
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
        return seqNo.getAndIncrement();
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
    public synchronized void setScheduledNewEvseStatus(EvseStatus scheduledNewEvseStatus) {
        this.scheduledNewEvseStatus = scheduledNewEvseStatus;
    }

    /**
     * Getter for scheduled evse status.
     *
     * @return scheduledNewEvseStatus
     */
    public synchronized EvseStatus getScheduledNewEvseStatus() {
        return this.scheduledNewEvseStatus;
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
     * Getter for evseStatus.
     *
     * @return evseStatus
     */
    public EvseStatus getEvseStatus() {
        return evseStatus;
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
     * Checks whether EVSE has a token or not.
     *
     * @return true if token does exist otherwise false
     */
    public boolean hasTokenId() {
        return isNotBlank(tokenId);
    }

    /**
     * Clear tokenId.
     */
    public void clearToken() {
        tokenId = StringUtils.EMPTY;
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
     * Getter for transaction.
     *
     * @return transaction
     */
    public EvseTransaction getTransaction() {
        return transaction;
    }

    /**
     * Plug connector.
     *
     * @param connectorId connector identity
     */
    public void plug(Integer connectorId) {

        Connector connector = findConnector(connectorId);
        connector.plug();
    }

    /**
     * Unplug connector.
     *
     * @param connectorId connector identity
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
        return connectors.stream().anyMatch(Connector::isCablePlugged);
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

    public List<Connector> getConnectors() {
        return connectors;
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

    private synchronized void changeEvseStatusIfScheduled() {
        if (nonNull(scheduledNewEvseStatus)) {
            changeStatus(scheduledNewEvseStatus);
            // clean
            scheduledNewEvseStatus = null;
        }
    }

}
