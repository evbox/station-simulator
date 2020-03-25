package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.common.MeterValue;
import com.evbox.everon.ocpp.v20.message.common.SampledValue;
import com.evbox.everon.ocpp.v20.message.station.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static java.math.BigDecimal.ZERO;

public class PayloadFactory {

    SignCertificateRequest createSignCertificateRequest(String csr) {
        return new SignCertificateRequest()
                    .withCsr(new CiString.CiString5500(csr))
                    .withTypeOfCertificate(SignCertificateRequest.TypeOfCertificate.CHARGING_STATION_CERTIFICATE);
    }

    AuthorizeRequest createAuthorizeRequest(String tokenId, List<Integer> evseIds) {
        AuthorizeRequest payload = new AuthorizeRequest();
        if (!evseIds.isEmpty()) {
            payload.setEvseId(evseIds);
        }

        IdToken idToken = new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443);
        payload.setIdToken(idToken);

        return payload;
    }

    StatusNotificationRequest createStatusNotification(int evseId, int connectorId, CableStatus cableStatus, Instant currentTime) {
        if (cableStatus == CableStatus.UNPLUGGED) {
            return createStatusNotification(evseId, connectorId, StatusNotificationRequest.ConnectorStatus.AVAILABLE, currentTime);
        }

        return createStatusNotification(evseId, connectorId, StatusNotificationRequest.ConnectorStatus.OCCUPIED, currentTime);
    }

    StatusNotificationRequest createStatusNotification(int evseId, int connectorId, StatusNotificationRequest.ConnectorStatus connectorStatus, Instant currentTime) {
        return new StatusNotificationRequest()
                    .withEvseId(evseId)
                    .withConnectorId(connectorId)
                    .withConnectorStatus(connectorStatus)
                    .withTimestamp(currentTime.atZone(ZoneOffset.UTC));
    }

    StatusNotificationRequest createStatusNotification(Evse evse, Connector connector, Instant currentTime) {
        StatusNotificationRequest payload = new StatusNotificationRequest();
        payload.setEvseId(evse.getId());
        payload.setConnectorId(connector.getId());
        payload.setConnectorStatus(connector.getConnectorStatus());
        payload.setTimestamp(currentTime.atZone(ZoneOffset.UTC));

        return payload;
    }

    BootNotificationRequest createBootNotification(BootNotificationRequest.Reason reason) {
        Modem modem = new Modem();
        modem.setIccid(new CiString.CiString20(StationHardwareData.MODEM_ICCID));
        modem.setImsi(new CiString.CiString20(StationHardwareData.MODEM_IMSI));

        ChargingStation chargingStation = new ChargingStation()
                .withModem(modem);

        chargingStation.setVendorName(new CiString.CiString50(StationHardwareData.VENDOR_NAME));
        chargingStation.setModel(new CiString.CiString20(StationHardwareData.MODEL));
        chargingStation.setSerialNumber(new CiString.CiString20(StationHardwareData.SERIAL_NUMBER));
        chargingStation.setFirmwareVersion(new CiString.CiString50(StationHardwareData.FIRMWARE_VERSION));

        return new BootNotificationRequest()
                .withReason(reason)
                .withChargingStation(chargingStation);
    }

    TransactionEventRequest createTransactionEventStart(Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId,
                                                        TransactionData.ChargingState chargingState, Integer remoteStartId,  Instant currentDateTime) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().toString()))
                .withRemoteStartId(remoteStartId)
                .withChargingState(chargingState);
        TransactionEventRequest payload = createTransactionEvent(evse.getId(), connectorId, reason, transactionData, STARTED, currentDateTime, evse.getSeqNoAndIncrement());

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventUpdate(Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId,
                                                         TransactionData.ChargingState chargingState, Instant currentDateTime) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().getTransactionId()))
                .withChargingState(chargingState);

        TransactionEventRequest payload = createTransactionEvent(evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.UPDATED, currentDateTime, evse.getSeqNoAndIncrement());

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventUpdate(Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId,
                                                         TransactionData.ChargingState chargingState, Instant currentDateTime, long powerConsumed) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().getTransactionId()))
                .withChargingState(chargingState);

        TransactionEventRequest payload = createTransactionEvent(evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.UPDATED, currentDateTime, evse.getSeqNoAndIncrement(), powerConsumed);

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventEnded(Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason,
                                                        TransactionData.StoppedReason stoppedReason, Instant currentDateTime) {

        TransactionData transactionData = new TransactionData().withId(new CiString.CiString36(evse.getTransaction().toString()))
                .withChargingState(TransactionData.ChargingState.SUSPENDED_EVSE)
                .withStoppedReason(stoppedReason);

        return createTransactionEvent(evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.ENDED, currentDateTime, evse.getSeqNoAndIncrement());
    }

    NotifyReportRequest createNotifyReportRequest(@Nullable Integer requestId, boolean tbc, int seqNo, ZonedDateTime generatedAt, List<ReportDatum> reportData) {
        NotifyReportRequest notifyReportRequest = new NotifyReportRequest()
                .withGeneratedAt(generatedAt)
                .withReportData(reportData)
                .withSeqNo(seqNo)
                .withTbc(tbc);

        notifyReportRequest.setRequestId(requestId);

        return notifyReportRequest;
    }

    private TransactionEventRequest createTransactionEvent(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData transactionData,
                                                           TransactionEventRequest.EventType eventType, Instant currentDateTime, Long seqNo) {
        List<MeterValue> meterValues = Collections.singletonList(new MeterValue().withTimestamp(ZonedDateTime.now(ZoneOffset.UTC)).withSampledValue(
                Collections.singletonList(new SampledValue().withValue(eventType == STARTED ? ZERO : new BigDecimal("1010")))));
        return createTransactionEvent(evseId, connectorId, reason, transactionData, eventType, currentDateTime, seqNo, meterValues);
    }

    private TransactionEventRequest createTransactionEvent(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData transactionData,
                                                           TransactionEventRequest.EventType eventType, Instant currentDateTime, Long seqNo, long powerConsumed) {
        List<MeterValue> meterValues = Collections.singletonList(new MeterValue().withTimestamp(ZonedDateTime.now(ZoneOffset.UTC)).withSampledValue(
                Collections.singletonList(new SampledValue().withValue(eventType == STARTED ? ZERO : new BigDecimal(powerConsumed)))));
        return createTransactionEvent(evseId, connectorId, reason, transactionData, eventType, currentDateTime, seqNo, meterValues);
    }

    private TransactionEventRequest createTransactionEvent(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData transactionData,
                                                           TransactionEventRequest.EventType eventType, Instant currentDateTime, Long seqNo, List<MeterValue> meterValues) {
        TransactionEventRequest transaction = new TransactionEventRequest();
        transaction.setEventType(eventType);
        transaction.setTimestamp(currentDateTime.atZone(ZoneOffset.UTC));
        transaction.setTriggerReason(reason);
        transaction.setSeqNo(seqNo);
        transaction.setTransactionData(transactionData);
        transaction.setEvse(new com.evbox.everon.ocpp.v20.message.common.Evse().withId(evseId).withConnectorId(connectorId));

        transaction.setMeterValue(meterValues);
        return transaction;
    }
}
