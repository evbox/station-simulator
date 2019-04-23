package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.google.common.collect.ImmutableList;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Represents a state of the station. This class is deliberately made thread-safe as it can be accessed by multiple-threads.
 *
 * @see StationMessageConsumer
 */
@ThreadSafe
public class StationState {

    @GuardedBy("this")
    private Clock clock = Clock.system(ZoneOffset.UTC);
    private volatile int heartbeatInterval;
    private final List<Evse> evses;

    public StationState(SimulatorConfiguration.StationConfiguration configuration) {
        this.evses = initEvses(configuration.getEvse().getCount(), configuration.getEvse().getConnectors());
    }

    public synchronized Instant getCurrentTime() {
        return clock.instant();
    }

    public void setCurrentTime(ZonedDateTime currentDateTime) {
        long currentTimeSeconds = currentDateTime.toEpochSecond();
        synchronized (this) {
            long stationTimeSeconds = clock.instant().getEpochSecond();
            clock = Clock.offset(clock, Duration.ofSeconds(currentTimeSeconds - stationTimeSeconds));
        }
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer unlockConnector(int evseId) {
        return findEvse(evseId).unlockConnector();
    }

    public void stopCharging(Integer evseId) {
        findEvse(evseId).stopCharging();
    }

    public boolean isCharging(Integer evseId) {
        return findEvse(evseId).isCharging();
    }

    public Evse getDefaultEvse() {
        return evses.get(0);
    }

    public boolean hasAuthorizedToken() {
        if (evses.size() > 1) {
            throw new IllegalStateException("One or more EVSE IDs have to be specified");
        }
        return hasAuthorizedToken(getDefaultEvse());
    }

    public boolean hasAuthorizedToken(Evse evse) {
        return isNotBlank(evse.getTokenId());
    }

    public void clearTokens() {
        evses.forEach(Evse::clearToken);
    }

    public void clearTransactions() {
        evses.forEach(Evse::stopTransaction);
    }

    public List<Integer> getEvseIds() {
        return evses.stream().map(Evse::getId).collect(toList());
    }

    public List<Evse> getEvses() {
        return evses;
    }

    public boolean hasOngoingTransaction(Integer evseId) {
        return findEvse(evseId).hasOngoingTransaction();
    }

    /**
     * Check if EVSE with given EVSE ID is present on the station.
     *
     * @param evseId EVSE identity
     * @return TRUE if station has EVSE with given identity, FALSE if it does not exist
     */
    public boolean hasEvse(int evseId) {
        return tryFindEvse(evseId).isPresent();
    }

    /**
     * Try to find EVSE by given EVSE ID or return empty result.
     *
     * @param evseId EVSE identity
     * @return optional with instance of {@link Evse} or Optional.empty()
     */
    public Optional<Evse> tryFindEvse(int evseId) {
        return evses.stream()
                .filter(evse -> evse.getId() == evseId)
                .findAny();
    }

    /**
     * Find an instance of {@link Evse} by evseId. If not found then throw {@link IllegalArgumentException}.
     *
     * @param evseId EVSE identity
     * @return an instance of {@link Evse}
     */
    public Evse findEvse(int evseId) {
        return tryFindEvse(evseId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("EVSE %s is not present", evseId)));
    }

    public Optional<Connector> tryFindConnector(int evseId, int connectorId) {
        return tryFindEvse(evseId)
                .flatMap(evse -> evse.getConnectors().stream()
                        .filter(connector -> connector.getId().equals(connectorId))
                        .findAny());
    }

    @Override
    public synchronized String toString() {
        return "StationState{" + "clock=" + clock + ", heartbeatInterval=" + heartbeatInterval + ", evses=" + evses + '}';
    }

    private List<Evse> initEvses(Integer evseCount, Integer connectorsPerEvseCount) {

        ImmutableList.Builder<Evse> evseListBuilder = ImmutableList.builder();

        for (int evseId = 1; evseId <= evseCount; evseId++) {
            ImmutableList.Builder<Connector> connectorListBuilder = ImmutableList.builder();
            for (int connectorId = 1; connectorId <= connectorsPerEvseCount; connectorId++) {
                connectorListBuilder.add(new Connector(connectorId, CableStatus.UNPLUGGED, AVAILABLE));
            }

            evseListBuilder.add(new Evse(evseId, connectorListBuilder.build()));
        }

        return evseListBuilder.build();
    }

}
