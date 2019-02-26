package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest.EventType.STARTED;
import static java.math.BigDecimal.ZERO;

public class PayloadFactory {

    AuthorizeRequest createAuthorizeRequest(String tokenId, List<Integer> evseIds) {
        AuthorizeRequest payload = new AuthorizeRequest();
        if (evseIds.size() > 0) {
            payload.setEvseId(evseIds);
        }

        IdToken idToken = new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdToken.Type.ISO_14443);
        payload.setIdToken(idToken);

        return payload;
    }

    StatusNotificationRequest createStatusNotification(int evseId, int connectorId, CableStatus cableStatus, Instant currentTime) {
        StatusNotificationRequest payload = new StatusNotificationRequest();
        payload.setEvseId(evseId);
        payload.setConnectorId(connectorId);

        if (cableStatus == CableStatus.UNPLUGGED) {
            payload.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        } else {
            payload.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.OCCUPIED);
        }

        payload.setTimestamp(currentTime.atZone(ZoneOffset.UTC));

        return payload;
    }

    StatusNotificationRequest createStatusNotification(Evse evse, Connector connector, Instant currentTime) {
        StatusNotificationRequest payload = new StatusNotificationRequest();
        payload.setEvseId(evse.getId());
        payload.setConnectorId(connector.getId());

        if (connector.getStatus() == CableStatus.UNPLUGGED) {
            payload.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.AVAILABLE);
        } else {
            payload.setConnectorStatus(StatusNotificationRequest.ConnectorStatus.OCCUPIED);
        }

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
            TransactionData.ChargingState chargingState, Instant currentDateTime) {

        TransactionData transactionData = new TransactionData()
                .withId(new CiString.CiString36(evse.getTransaction().toString()))
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
                .withId(new CiString.CiString36(Integer.toString(evse.getTransaction().getTransactionId())))
                .withChargingState(chargingState);

        TransactionEventRequest payload = createTransactionEvent(evse.getId(), connectorId, reason, transactionData, TransactionEventRequest.EventType.UPDATED, currentDateTime, evse.getSeqNoAndIncrement());

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

    private TransactionEventRequest createTransactionEvent(Integer evseId, Integer connectorId, TransactionEventRequest.TriggerReason reason, TransactionData transactionData,
            TransactionEventRequest.EventType eventType, Instant currentDateTime, Long seqNo) {
        TransactionEventRequest transaction = new TransactionEventRequest();
        transaction.setEventType(eventType);
        transaction.setTimestamp(currentDateTime.atZone(ZoneOffset.UTC));
        transaction.setTriggerReason(reason);
        transaction.setSeqNo(seqNo);
        transaction.setTransactionData(transactionData);
        transaction.setEvse(new com.evbox.everon.ocpp.v20.message.station.Evse().withId(evseId).withConnectorId(connectorId));

        transaction.setMeterValue(Collections.singletonList(new MeterValue().withTimestamp(ZonedDateTime.now()).withSampledValue(
                Collections.singletonList(new SampledValue().withValue(eventType == STARTED ? ZERO : new BigDecimal("1010"))))));
        return transaction;
    }
}
