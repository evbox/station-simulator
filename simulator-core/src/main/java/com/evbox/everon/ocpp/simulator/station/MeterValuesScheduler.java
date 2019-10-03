package com.evbox.everon.ocpp.simulator.station;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@FieldDefaults(makeFinal = true)
public class MeterValuesScheduler {

    private static final int INITIAL_TASK_DELAY_IN_SECONDS = 2;
    private static final int TASK_PERIOD_IN_SECONDS = 1;

    private MeterValuesSenderTask meterValuesSenderTask;

    public MeterValuesScheduler(StationPersistenceLayer stationPersistenceLayer, StationMessageSender stationMessageSender, long sendMeterValuesInterval, long powerConsumption) {
        this.meterValuesSenderTask = new MeterValuesSenderTask(stationPersistenceLayer, stationMessageSender, sendMeterValuesInterval, powerConsumption);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                meterValuesSenderTask, INITIAL_TASK_DELAY_IN_SECONDS, TASK_PERIOD_IN_SECONDS, TimeUnit.MILLISECONDS);
    }

}
