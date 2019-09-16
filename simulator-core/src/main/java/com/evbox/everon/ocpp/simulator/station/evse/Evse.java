package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.simulator.station.evse.Connector.ConnectorView;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction.EvseTransactionView;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.evbox.everon.ocpp.simulator.station.evse.EvseTransactionStatus.*;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
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

    private ChargingStopReason stopReason = ChargingStopReason.NONE;
    private EvseStatus evseStatus;
    private EvseTransaction transaction;
    /**
     * If nonNull should be applied when transaction stops
     */
    private EvseStatus scheduledNewEvseStatus;

    /**
     *  Total power consumed by the evse in watt hour
     */
    private long totalConsumedWattHours;

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
        stopReason = ChargingStopReason.LOCALLY_STOPPED;
    }

    /**
     * Remotely stop charging.
     */
    public void remotelyStopCharging() {
        charging = false;
        stopReason = ChargingStopReason.REMOTELY_STOPPED;
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
    public void createTransaction(String transactionId) {
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
     *
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
     * TRIES to find any LOCKED connector and switch to PLUGGED status.
     *
     * @return identity of the connector or zero.
     */
    public Integer tryUnlockConnector() {

        Connector lockedConnector = connectors.stream()
                .filter(Connector::isCableLocked)
                .findAny()
                .orElse(null);

        if (lockedConnector != null) {
            lockedConnector.unlock();
            return lockedConnector.getId();
        }

        return 0;
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
     * Find an instance of an available {@link Connector}.
     *
     * @return optional {@link Connector} instance
     */
    public Optional<Connector> tryFindAvailableConnector() {
        return connectors.stream()
                .filter(connector -> StatusNotificationRequest.ConnectorStatus.AVAILABLE.equals(connector.getConnectorStatus()))
                .findAny();
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

    /**
     * Increases the power consumed by the value specified.
     * If the new value exceeds MAX_VALUE then it will restart from 0.
     *
     * @param incrementValue amount of power to add to the consumed power
     * @return updated value of consumed power
     */
    public long incrementPowerConsumed(long incrementValue) {
        long diff = Long.MAX_VALUE - totalConsumedWattHours;
        if (incrementValue > diff) {
            totalConsumedWattHours = incrementValue - diff - 1;
        } else {
            totalConsumedWattHours += incrementValue;
        }
        return totalConsumedWattHours;
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
                ", totalConsumedWattHours=" + totalConsumedWattHours +
                '}';
    }

    public EvseView createView() {

        List<ConnectorView> connectorViews = connectors.stream().map(Connector::createView).collect(toList());

        return EvseView.builder()
                .id(id)
                .connectors(connectorViews)
                .tokenId(tokenId)
                .charging(charging)
                .seqNo(seqNo)
                .evseStatus(evseStatus)
                .transaction(transaction.createView())
                .scheduledNewEvseStatus(scheduledNewEvseStatus)
                .totalConsumedWattHours(totalConsumedWattHours)
                .build();
    }

    private void changeEvseStatusIfScheduled() {
        if (nonNull(scheduledNewEvseStatus)) {
            changeStatus(scheduledNewEvseStatus);
            // clean
            scheduledNewEvseStatus = null;
        }
    }

    @Getter
    @Builder
    public static class EvseView {

        private final int id;
        private final List<ConnectorView> connectors;
        private final String tokenId;
        private final boolean charging;
        private final long seqNo;
        private final EvseStatus evseStatus;
        private final EvseTransactionView transaction;
        private final EvseStatus scheduledNewEvseStatus;
        private final long totalConsumedWattHours;

        /**
         * Checks whether EVSE has a token or not.
         *
         * @return true if token does exist otherwise false
         */
        public boolean hasTokenId() {
            return isNotBlank(tokenId);
        }

        /**
         * Check whether transaction is ongoing or not.
         *
         * @return `true` in case if ongoing `false` otherwise
         */
        public boolean hasOngoingTransaction() {
            return transaction.getStatus() == IN_PROGRESS;
        }

    }

}
