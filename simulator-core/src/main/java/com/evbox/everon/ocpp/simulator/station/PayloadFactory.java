package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.common.MeterValue;
import com.evbox.everon.ocpp.v20.message.common.SampledValue;
import com.evbox.everon.ocpp.v20.message.common.SignedMeterValue;
import com.evbox.everon.ocpp.v20.message.station.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.UPDATED;
import static java.math.BigDecimal.ZERO;

public class PayloadFactory {

    private static final String EICHRECHT = "EICHRECHT";
    private static final String SIGNED_METER_START = "AP;0;3;ALCV3ABBBISHMA2RYEGAZE3HV5YQBQRQAEHAR2MN;BIHEIWSHAAA2W2V7OYYDCNQAAAFACRC2I4ADGAETI4AAAABAOOJYUAGMXEGV4AIAAEEAB7Y6AAO3EAIAAAAAAABQGQ2UINJZGZATGMJTGQ4DAAAAAAAAAACXAAAABKYAAAAA====;R7KGQ3CEYTZI6AWKPOA42MXJTGBW27EUE2E6X7J77J5WMQXPSOM3E27NMVM2D77DPTMO3YACIPTRI===;";
    private static final String SIGNED_METER_STOP = "AP;1;3;ALCV3ABBBISHMA2RYEGAZE3HV5YQBQRQAEHAR2MN;BIHEIWSHAAA2W2V7OYYDCNQAAAFACRC2I4ADGAETI4AAAAAQHCKIUAETXIGV4AIAAEEAB7Y6ACT3EAIAAAAAAABQGQ2UINJZGZATGMJTGQ4DAAAAAAAAAACXAAAABLAAAAAA====;HIWX6JKGDO3JARYVAQYKJO6XB7HFLMLNUUCDBOTWJFFC7IY3VWZGN7UPIVA26TMTK4S2GVXJ3BD4S===;";

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

    TransactionEventRequest createTransactionEventStart(String stationId, Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId,
                                                        TransactionData.ChargingState chargingState, Integer remoteStartId,  Instant currentDateTime) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().toString()))
                .withRemoteStartId(remoteStartId)
                .withChargingState(chargingState);
        TransactionEventRequest payload = createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, STARTED, currentDateTime, evse.getSeqNoAndIncrement(), 0L);

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventUpdate(String stationId, Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason, String tokenId,
                                                         TransactionData.ChargingState chargingState, Instant currentDateTime, long powerConsumed) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().getTransactionId()))
                .withChargingState(chargingState);

        TransactionEventRequest payload = createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.UPDATED, currentDateTime, evse.getSeqNoAndIncrement(), powerConsumed);

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventEnded(String stationId, Evse evse, Integer connectorId, TransactionEventRequest.TriggerReason reason,
                                                        TransactionData.StoppedReason stoppedReason, Instant currentDateTime, long powerConsumed) {

        TransactionData transactionData = new TransactionData().withId(new CiString.CiString36(evse.getTransaction().toString()))
                .withChargingState(TransactionData.ChargingState.SUSPENDED_EVSE)
                .withStoppedReason(stoppedReason);

        return createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.ENDED, currentDateTime, evse.getSeqNoAndIncrement(), powerConsumed);
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

    private TransactionEventRequest createTransactionEvent(String stationId, Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData transactionData,
                                                           TransactionEventRequest.EventType eventType, Instant currentDateTime, Long seqNo, long powerConsumed) {
        SampledValue sampledValue = new SampledValue()
                                        .withSignedMeterValue(createSignedMeterValues(stationId, eventType, powerConsumed))
                                        .withValue(eventType == STARTED ? ZERO : new BigDecimal(powerConsumed));
        MeterValue meterValue = new MeterValue()
                                    .withTimestamp(ZonedDateTime.now(ZoneOffset.UTC))
                                    .withSampledValue(Collections.singletonList(sampledValue));
        List<MeterValue> meterValues = Collections.singletonList(meterValue);
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

    private SignedMeterValue createSignedMeterValues(String stationId, TransactionEventRequest.EventType eventType, Long powerConsumed) {
        if (stationId.toUpperCase().contains(EICHRECHT) && eventType != UPDATED) {
            SignedMeterValue signedMeterValue =  new SignedMeterValue()
                                                    .withEncodingMethod(SignedMeterValue.EncodingMethod.OTHER)
                                                     .withEncodedMeterValue(new CiString.CiString512(powerConsumed.toString()))
                                                    .withSignatureMethod(SignedMeterValue.SignatureMethod.ECDSA_192_SHA_256);

            if (eventType == STARTED) {
                signedMeterValue.withMeterValueSignature(new CiString.CiString2500(SIGNED_METER_START));
            } else {
                signedMeterValue.withMeterValueSignature(new CiString.CiString2500(SIGNED_METER_STOP));
            }

            return signedMeterValue;
        }
        return null;
    }
}
