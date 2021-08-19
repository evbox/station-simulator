package com.evbox.everon.ocpp.simulator.station.schedulers;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v201.message.station.ChargingState;
import com.evbox.everon.ocpp.v201.message.station.TriggerReason;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Background task that will send meter values during ongoing transactions
 */
@Slf4j
public class MeterValuesSenderTask implements Runnable {

    private final StationStore stationStore;
    private final StationMessageSender stationMessageSender;

    private long sendMeterValuesIntervalSec;
    private long powerIncreasedPerInterval;
    private Map<Integer, LocalDateTime> timeOfLastMeterValuePerEVSE;

    public MeterValuesSenderTask(StationStore stationStore, StationMessageSender stationMessageSender, long sendMeterValuesIntervalSec, long consumptionWattHour) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
        this.sendMeterValuesIntervalSec = sendMeterValuesIntervalSec;
        this.timeOfLastMeterValuePerEVSE = new HashMap<>();
        this.powerIncreasedPerInterval = sendMeterValuesIntervalSec * consumptionWattHour / 3600;
    }

    @Override
    public void run() {
        try {
            LocalDateTime now = LocalDateTime.now();
            for (Evse evse : stationStore.getEvses()) {
                if (shouldSendMeterValue(now, evse.getId(), stationStore.createView())) {
                    long powerUsed = evse.incrementPowerConsumed(powerIncreasedPerInterval);
                    stationMessageSender.sendTransactionEventUpdate(evse.getId(), null, TriggerReason.METER_VALUE_PERIODIC, ChargingState.CHARGING, powerUsed);
                    timeOfLastMeterValuePerEVSE.put(evse.getId(), now);
                }
            }
        } catch (Exception e) {
            log.error("Metervalues send exception", e);
        }
    }

    private boolean shouldSendMeterValue(LocalDateTime now, int evseId, StationStore.StationStoreView stationStoreView) {
        LocalDateTime timeToSendMeterValues = timeOfLastMeterValuePerEVSE.getOrDefault(evseId, LocalDateTime.MIN).plus(sendMeterValuesIntervalSec, ChronoUnit.SECONDS);
        return sendMeterValuesIntervalSec > 0 && stationStoreView.isCharging(evseId) && timeToSendMeterValues.isBefore(now);
    }
}
