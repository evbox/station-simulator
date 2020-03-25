package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.centralserver.Component;
import com.evbox.everon.ocpp.v20.message.centralserver.Variable;
import com.evbox.everon.ocpp.v20.message.station.EventDatum;
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

    private EventDatum generateEventDatum() {
        return new EventDatum()
                .withEventId(ThreadLocalRandom.current().nextInt(100))
                .withTimestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .withTrigger(EventDatum.Trigger.ALERTING)
                .withActualValue(new CiString.CiString1000("999"))
                .withCleared(true)
                .withComponent(new Component().withName(new CiString.CiString50("component")))
                .withVariable(new Variable().withName(new CiString.CiString50("variable")));
    }
}
