package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@AllArgsConstructor
public class StationState {

    private Clock clock = Clock.system(ZoneOffset.UTC);
    private int heartbeatInterval;
    private List<Evse> evses;

    public StationState(SimulatorConfiguration.StationConfiguration configuration) {
        this.evses = initEvses(configuration.getEvse().getCount(), configuration.getEvse().getConnectors());
    }

    public static StationState copyOf(StationState stationState) {
        List<Evse> evsesCopy = stationState.evses.stream().map(Evse::copyOf).collect(toList());

        return new StationState(Clock.offset(stationState.clock, Duration.ZERO),
                stationState.heartbeatInterval, evsesCopy);
    }

    public Instant getCurrentTime() {
        return clock.instant();
    }

    public void setCurrentTime(ZonedDateTime currentDateTime) {
        long currentTimeSeconds = currentDateTime.toEpochSecond();
        long stationTimeSeconds = clock.instant().getEpochSecond();

        clock = Clock.offset(clock, Duration.ofSeconds(currentTimeSeconds - stationTimeSeconds));
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer lockConnector(int evseId) {
        return findEvse(evseId).lockPluggedConnector();
    }

    public Integer unlockConnector(int evseId) {
        return findEvse(evseId).unlockConnector();
    }

    public void plug(Integer connectorId) {
        Evse.Connector connector = findConnector(connectorId);
        connector.plug();
    }

    public void unplug(Integer connectorId) {
        Evse.Connector connector = findConnector(connectorId);
        connector.unplug();
    }

    private List<Evse> initEvses(Integer evseCount, Integer connectorsPerEvseCount) {
        int connectorId = 0;

        ImmutableList.Builder<Evse> evseListBuilder = ImmutableList.builder();

        for (int i = 1; i <= evseCount; i++) {
            ImmutableList.Builder<Evse.Connector> connectorListBuilder = ImmutableList.builder();
            for (int j = 0; j < connectorsPerEvseCount; j++) {
                connectorListBuilder.add(new Evse.Connector(++connectorId, Evse.ConnectorState.AVAILABLE));
            }

            evseListBuilder.add(new Evse(i, connectorListBuilder.build(), StringUtils.EMPTY, false, 0L, null));
        }

        return evseListBuilder.build();
    }

    public Integer startCharging(Integer evseId) {
        return findEvse(evseId).startCharging();
    }

    public void stopCharging(Integer evseId) {
        findEvse(evseId).stopCharging();
    }

    public boolean isCharging(Integer evseId) {
        return findEvse(evseId).isCharging();
    }

    public boolean isPlugged(Integer evseId) {
        return !isCharging(evseId) && findEvse(evseId).getConnectors().stream().anyMatch(Evse.Connector::isPlugged);
    }

    public Evse.ConnectorState getConnectorState(int connectorId) {
        return findConnector(connectorId).getState();
    }

    public Long getSeqNo(int evseId) {
        return findEvse(evseId).getSeqNo();
    }

    public Integer findEvseId(int connectorId) {
        return findEvseByConnectorId(connectorId).getId();
    }

    public void storeToken(Integer evseId, String tokenId) {
        findEvse(evseId).storeToken(tokenId);
    }

    public Integer getDefaultEvseId() {
        return evses.get(0).getId();
    }

    public boolean hasAuthorizedToken() {
        if (evses.size() > 1) {
            throw new IllegalStateException("One or more EVSE IDs have to be specified");
        }
        return hasAuthorizedToken(getDefaultEvseId());
    }

    public boolean hasAuthorizedToken(Integer evseId) {
        return isNotBlank(findEvse(evseId).getTokenId());
    }

    public String getToken(Integer evseId) {
        String token = findEvse(evseId).getTokenId();
        if (isBlank(token)) {
            throw new IllegalStateException(String.format("Token is not authorized yet: %s", token));
        }
        return token;
    }

    public void clearToken(Integer evseId) {
        findEvse(evseId).resetToken();
    }

    public void clearTokens() {
        evses.forEach(Evse::resetToken);
    }

    public String getTransactionId(Integer evseId) {
        return findEvse(evseId).getTransactionId().toString();
    }

    public void setTransactionId(Integer evseId, Integer transactionId) {
        findEvse(evseId).setTransactionId(transactionId);
    }

    public void clearTransactionId(Integer evseId) {
        findEvse(evseId).clearTransactionId();
    }

    public void clearTransactions() {
        evses.forEach(Evse::clearTransactionId);
    }

    public List<Integer> getEvses() {
        return evses.stream().map(Evse::getId).collect(toList());
    }

    public boolean hasOngoingTransaction(Integer evseId) {
        return findEvse(evseId).hasOngoingTransaction();
    }

    private Evse.Connector findConnector(int connectorId) {
        return evses.stream()
            .flatMap(evse -> evse.getConnectors().stream())
            .filter(connector -> connector.getId().equals(connectorId))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException(String.format("No connector with ID: %s", connectorId)));
    }

    private Evse findEvseByConnectorId(int connectorId) {
        return evses.stream()
                .filter(evse -> evse.getConnectors().stream().anyMatch(connector -> connector.getId().equals(connectorId)))
                .findAny().orElseThrow(() -> new IllegalArgumentException(String.format("Connector %s is not present", connectorId)));
    }

    private Evse findEvse(int evseId) {
        return evses.stream()
                .filter(evse -> evse.getId().equals(evseId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("EVSE %s is not present", evseId)));
    }

    @Override
    public String toString() {
        return "StationState{" + "clock=" + clock + ", heartbeatInterval=" + heartbeatInterval + ", evses=" + evses + '}';
    }
}
