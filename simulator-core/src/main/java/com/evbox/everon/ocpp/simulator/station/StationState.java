package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.ConnectorStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;

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
        Connector connector = findConnector(connectorId);
        connector.plug();
    }

    public void unplug(Integer connectorId) {
        Connector connector = findConnector(connectorId);
        connector.unplug();
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
        return !isCharging(evseId) && findEvse(evseId).getConnectors().stream().anyMatch(Connector::isPlugged);
    }

    public ConnectorStatus getConnectorState(int connectorId) {
        return findConnector(connectorId).getStatus();
    }

    public Integer findEvseId(int connectorId) {
        return findEvseByConnectorId(connectorId).getId();
    }

    public void storeToken(Integer evseId, String tokenId) {
        findEvse(evseId).setToken(tokenId);
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
        findEvse(evseId).clearToken();
    }

    public void clearTokens() {
        evses.forEach(Evse::clearToken);
    }

    public void clearTransactions() {
        evses.forEach(Evse::stopTransaction);
    }

    public List<Integer> getEvses() {
        return evses.stream().map(Evse::getId).collect(toList());
    }

    public boolean hasOngoingTransaction(Integer evseId) {
        return findEvse(evseId).hasOngoingTransaction();
    }

    /**
     * Find an instance of {@link Evse} by evseId. If not found then throw {@link IllegalArgumentException}.
     *
     * @param evseId evse identity
     * @return an instance of {@link Evse}
     */
    public Evse findEvse(int evseId) {
        return evses.stream()
                .filter(evse -> evse.getId() == evseId)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("EVSE %s is not present", evseId)));
    }

    @Override
    public String toString() {
        return "StationState{" + "clock=" + clock + ", heartbeatInterval=" + heartbeatInterval + ", evses=" + evses + '}';
    }

    private List<Evse> initEvses(Integer evseCount, Integer connectorsPerEvseCount) {

        ImmutableList.Builder<Evse> evseListBuilder = ImmutableList.builder();

        for (int evseId = 1; evseId <= evseCount; evseId++) {
            ImmutableList.Builder<Connector> connectorListBuilder = ImmutableList.builder();
            for (int connectorId = 1; connectorId <= connectorsPerEvseCount; connectorId++) {
                connectorListBuilder.add(new Connector(connectorId, ConnectorStatus.UNPLUGGED));
            }

            evseListBuilder.add(new Evse(evseId, connectorListBuilder.build()));
        }

        return evseListBuilder.build();
    }

    private Connector findConnector(int connectorId) {
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

}
