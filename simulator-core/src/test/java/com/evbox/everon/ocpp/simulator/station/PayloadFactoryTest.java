package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.v201.message.centralserver.DataTransferRequest;
import com.evbox.everon.ocpp.v201.message.station.Component;
import com.evbox.everon.ocpp.v201.message.station.EventData;
import com.evbox.everon.ocpp.v201.message.station.EventNotification;
import com.evbox.everon.ocpp.v201.message.station.EventTrigger;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static com.evbox.everon.ocpp.simulator.station.PayloadFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PayloadFactoryTest {
    private final static int evseId = 1;
    private final static int connectorId = 2;
    private final static String errorCode = "0x0102";
    private final static String errorDescription = "Auto-recoverable DC Leakage detected";
    private final static Instant timestamp = Instant.now();

    PayloadFactory payloadFactory = new PayloadFactory();

    @Test
    void shouldBuildDataTransferRequest() {
        List<Integer> connectorIds = List.of(1, 2, 3);
        DataTransferRequest request = payloadFactory.createPublicKeyDataTransfer(connectorIds);
        assertThat(request.getVendorId()).isEqualTo(GENERAL_CONFIGURATION);
        assertThat(request.getMessageId()).isEqualTo(SET_METER_CONFIGURATION);
        assertThat(String.valueOf(request.getData())).contains(PUBLIC_KEY);
    }

    @Test
    void shouldBuildProblemEventData() {
        EventData eventData = payloadFactory.createProblemEventData(evseId, connectorId, errorCode, errorDescription, timestamp);

        assertEquals(EventNotification.HARD_WIRED_MONITOR, eventData.getEventNotificationType());
        assertEquals(errorCode, eventData.getTechCode().toString());
        assertEquals(timestamp.atZone(ZoneOffset.UTC), eventData.getTimestamp());
        assertEquals("Problem", eventData.getVariable().getName().toString());
        assertNotNull(eventData.getEventId());
        assertFalse(eventData.getCleared());
        assertEquals(EventTrigger.DELTA, eventData.getTrigger());
        assertEquals("true", eventData.getActualValue().toString());

        Component component = eventData.getComponent();
        assertEquals("1", component.getInstance().toString());
        assertEquals("EVSE", component.getName().toString());
        assertEquals(evseId, component.getEvse().getId());
        assertEquals(connectorId, component.getEvse().getConnectorId());

        assertEquals(errorDescription, eventData.getTechInfo().toString());
    }

    @Test
    void shouldBuildProblemEventDataWithoutConnector() {
        EventData eventData = payloadFactory.createProblemEventData(evseId, 0, errorCode, errorDescription, timestamp);

        assertNull(eventData.getComponent().getEvse().getConnectorId());
    }

    @Test
    void shouldBuildProblemEventDataWithoutEvse() {
        EventData eventData = payloadFactory.createProblemEventData(0, 0, errorCode, errorDescription, timestamp);

        assertEquals("Station", eventData.getComponent().getName().toString());
        assertNull(eventData.getComponent().getEvse());
    }

    @Test
    void shouldBuildProblemEventDataWithoutDescription() {
        EventData eventData = payloadFactory.createProblemEventData(evseId, connectorId, errorCode, null, timestamp);

        assertNull(eventData.getTechInfo());
    }
}
