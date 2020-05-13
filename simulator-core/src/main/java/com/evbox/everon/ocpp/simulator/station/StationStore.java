package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.Evse.EvseView;
import com.evbox.everon.ocpp.simulator.station.exceptions.StationException;
import com.evbox.everon.ocpp.v20.message.station.ConnectionData;
import com.evbox.everon.ocpp.v20.message.station.Reservation;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.*;
import java.util.*;

import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Represents the state of the station.
 */
@Slf4j
@ToString
public class StationStore {

    private String stationId;
    private Clock clock = Clock.system(ZoneOffset.UTC);
    private int heartbeatInterval;
    private int evConnectionTimeOut;
    private Map<Integer, Evse> evses;
    private OptionList<TxStartStopPointVariableValues> txStartPointValues;
    private OptionList<TxStartStopPointVariableValues> txStopPointValues;

    private X509Certificate stationCertificate;
    private List<X509Certificate> stationCertificateChain;
    private PublicKey stationPublicKey;
    private PrivateKey stationPrivateKey;
    private Map<Integer, ConnectionData> networkConnectionProfiles = new HashMap<>();
    private List<Integer> networkConfigurationPriority = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    public StationStore(SimulatorConfiguration.StationConfiguration configuration) {
        this.stationId = configuration.getId();
        this.evses = initEvses(configuration.getEvse().getCount(), configuration.getEvse().getConnectors(), configuration.getEvse().getConnectorStatus());
        this.evConnectionTimeOut = configuration.getComponentsConfiguration().getTxCtrlr().getEvConnectionTimeOutSec();
        this.txStartPointValues = new OptionList<>(TxStartStopPointVariableValues.fromValues(configuration.getComponentsConfiguration().getTxCtrlr().getTxStartPoints()));
        this.txStopPointValues = new OptionList<>(TxStartStopPointVariableValues.fromValues(configuration.getComponentsConfiguration().getTxCtrlr().getTxStopPoints()));
    }

    public StationStore(Clock clock, int heartbeatInterval, int evConnectionTimeOut, Map<Integer, Evse> evses) {
        this.clock = clock;
        this.heartbeatInterval = heartbeatInterval;
        this.evConnectionTimeOut = evConnectionTimeOut;
        this.evses = evses;
    }

    public String getStationId() {
        return stationId;
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

    public int getEVConnectionTimeOut() {
        return evConnectionTimeOut;
    }

    public void setEVConnectionTimeOut(int evConnectionTimeOut) {
        this.evConnectionTimeOut = evConnectionTimeOut;
    }

    public X509Certificate getStationCertificate() {
        return stationCertificate;
    }

    public void setStationCertificate(X509Certificate stationCertificate) {
        this.stationCertificate = stationCertificate;
    }

    public List<X509Certificate> getStationCertificateChain() {
        return stationCertificateChain;
    }

    public void setStationCertificateChain(List<X509Certificate> stationCertificateChain) {
        this.stationCertificateChain = Collections.unmodifiableList(stationCertificateChain);
    }

    public PublicKey getStationPublicKey() {
        return stationPublicKey;
    }

    public void setStationPublicKey(PublicKey stationPublicKey) {
        this.stationPublicKey = stationPublicKey;
    }

    public PrivateKey getStationPrivateKey() {
        return stationPrivateKey;
    }

    public void setStationPrivateKey(PrivateKey stationPrivateKey) {
        this.stationPrivateKey = stationPrivateKey;
    }

    public Integer unlockConnector(int evseId) {
        return findEvse(evseId).unlockConnector();
    }

    public void stopCharging(Integer evseId) {
        findEvse(evseId).stopCharging();
    }

    public Evse getDefaultEvse() {
        return evses.values().stream().findFirst().orElseThrow(() -> new StationException("No evse was found"));
    }

    public void clearTokens() {
        evses.values().forEach(Evse::clearToken);
    }

    public void clearTransactions() {
        evses.values().forEach(Evse::stopTransaction);
    }

    public List<Integer> getEvseIds() {
        return new ArrayList<>(evses.keySet());
    }

    public List<Evse> getEvses() {
        return new ArrayList<>(evses.values());
    }

    public void addNetworkConnectionProfile(Integer configurationSlot, ConnectionData data) { networkConnectionProfiles.put(configurationSlot, data); }

    public Map<Integer, ConnectionData> getNetworkConnectionProfiles() {
        return networkConnectionProfiles;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
    }

    public List<Reservation> getReservations() {
        return reservations;
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
        return evses.containsKey(evseId);
    }

    /**
     * Try to find EVSE by given EVSE ID or return empty result.
     *
     * @param evseId EVSE identity
     * @return optional with instance of {@link Evse} or Optional.empty()
     */
    public Optional<Evse> tryFindEvse(int evseId) {
        return Optional.ofNullable(evses.get(evseId));
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

    /**
     * Try to find EVSE by given TRANSACTION ID or return empty result.
     *
     * @param transactionId Transaction identity
     * @return optional with instance of {@link Evse} or Optional.empty()
     */
    public Optional<Evse> tryFindEvseByTransactionId(String transactionId) {
        return evses.values()
                    .stream()
                    .filter(evse -> evse.hasOngoingTransaction() && evse.getTransaction().getTransactionId().equalsIgnoreCase(transactionId))
                    .findAny();
    }

    /**
     * Try to find an EVSE with no ongoing transaction.
     *
     * @return optional with instance of {@link Evse} or Optional.empty()
     */
    public Optional<Evse> tryFindAvailableEvse() {
        return evses.values()
                    .stream()
                    .filter(evse -> !evse.hasOngoingTransaction())
                    .findAny();
    }

    public Optional<Reservation> tryFindReservationById(int reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(reservationId))
                .findAny();
    }

    public Optional<Connector> tryFindConnector(int evseId, int connectorId) {
        return tryFindEvse(evseId)
                .flatMap(evse -> evse.getConnectors().stream()
                        .filter(connector -> connector.getId().equals(connectorId))
                        .findAny());
    }

    public OptionList<TxStartStopPointVariableValues> getTxStartPointValues() {
        return txStartPointValues;
    }

    public void setTxStartPointValues(OptionList<TxStartStopPointVariableValues> txStartPointValues) {
        this.txStartPointValues = txStartPointValues;
    }

    public OptionList<TxStartStopPointVariableValues> getTxStopPointValues() {
        return txStopPointValues;
    }

    public void setTxStopPointValues(OptionList<TxStartStopPointVariableValues> txStopPointValues) {
        this.txStopPointValues = txStopPointValues;
    }

    public void addNetworkConfigurationPriority(Integer networkConfigurationPriority) {
        this.networkConfigurationPriority.add(networkConfigurationPriority);
    }

    public List<Integer> getNetworkConfigurationPriority() {
        return this.networkConfigurationPriority;
    }

    private Map<Integer, Evse> initEvses(Integer evseCount, Integer connectorsPerEvseCount, StatusNotificationRequest.ConnectorStatus connectorStatus) {

        ImmutableMap.Builder<Integer, Evse> evseMapBuilder = ImmutableMap.builder();

        for (int evseId = 1; evseId <= evseCount; evseId++) {
            ImmutableList.Builder<Connector> connectorListBuilder = ImmutableList.builder();
            for (int connectorId = 1; connectorId <= connectorsPerEvseCount; connectorId++) {
                connectorListBuilder.add(new Connector(connectorId, CableStatus.UNPLUGGED, connectorStatus));
            }

            evseMapBuilder.put(evseId, new Evse(evseId, connectorListBuilder.build()));
        }

        return evseMapBuilder.build();
    }

    public StationStoreView createView() {
        List<EvseView> evsesCopy = evses.values().stream().map(Evse::createView).collect(toList());

        return new StationStoreView(clock, heartbeatInterval, evConnectionTimeOut, stationCertificate, evsesCopy);
    }

    @Getter
    @AllArgsConstructor
    public class StationStoreView {

        @JsonIgnore
        private final Clock clock;
        private final int heartbeatInterval;
        private final int evConnectionTimeOut;
        private final X509Certificate stationCertificate;
        private final List<EvseView> evses;

        public boolean hasAuthorizedToken() {
            if (evses.size() > 1) {
                throw new IllegalStateException("One or more EVSE IDs have to be specified");
            }
            return hasAuthorizedToken(getDefaultEvse());
        }

        public boolean hasAuthorizedToken(EvseView evse) {
            return isNotBlank(evse.getTokenId());
        }

        public boolean hasOngoingTransaction(Integer evseId) {
            return findEvse(evseId).hasOngoingTransaction();
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
        public Instant getCurrentTime() {
            return clock.instant();
        }

        @JsonIgnore
        public EvseView getDefaultEvse() {
            return evses.get(0);
        }

        @JsonIgnore
        public String getStationCertificate() {
            if (stationCertificate == null) {
                return "No certificate found!";
            }

            String certificate = "";
            try (StringWriter stringWriter = new StringWriter(); JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
                pemWriter.writeObject(stationCertificate);
                pemWriter.flush();
                certificate = stringWriter.toString();
            } catch (IOException e) {
                log.debug("Error printing the certificate", e);
            }

            return certificate;
        }

        public boolean isCharging(Integer evseId) {
            return findEvse(evseId).isCharging();
        }

        /**
         * Find an instance of {@link Evse} by evseId. If not found then throw {@link IllegalArgumentException}.
         *
         * @param evseId EVSE identity
         * @return an instance of {@link Evse}
         */
        public EvseView findEvse(int evseId) {
            return tryFindEvse(evseId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("EVSE %s is not present", evseId)));
        }

        /**
         * Try to find EVSE by given EVSE ID or return empty result.
         *
         * @param evseId EVSE identity
         * @return optional with instance of {@link Evse} or Optional.empty()
         */
        public Optional<EvseView> tryFindEvse(int evseId) {
            return evses.stream()
                    .filter(evse -> evse.getId() == evseId)
                    .findAny();
        }

        @Override
        public String toString() {
            ObjectWriter prettyJSONWriter = new ObjectMapper()
                                                    .findAndRegisterModules()
                                                    .writerWithDefaultPrettyPrinter();
            try {
                return prettyJSONWriter.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return "Error while serializing StationStoreView: " + e.getMessage();
            }
        }
    }
}
