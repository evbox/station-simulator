package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v201.message.station.*;
import lombok.AllArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class AlertEventSenderTask implements Runnable {

    private final StationMessageSender stationMessageSender;

    @Override
    public void run() {
        stationMessageSender.sendNotifyEvent(Collections.singletonList(generateEventDatum()));
    }

    private EventData generateEventDatum() {
        return new EventData()
                .withEventId(ThreadLocalRandom.current().nextInt(100))
                .withTimestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .withTrigger(EventTrigger.ALERTING)
                .withActualValue(new CiString.CiString2500("999"))
                .withCleared(true)
                .withComponent(new Component().withName(new CiString.CiString50("component")))
                .withEventNotificationType(EventNotification.CUSTOM_MONITOR)
                .withVariable(new Variable().withName(new CiString.CiString50("variable")));
    }
}
