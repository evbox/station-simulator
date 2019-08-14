package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
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

    private final StationState stationState;
    private final StationMessageSender stationMessageSender;

    private long sendMeterValuesIntervalSec;
    private long powerIncreasedPerInterval;
    private Map<Integer, LocalDateTime> timeOfLastMeterValuePerEVSE;

    public MeterValuesSenderTask(StationState stationState, StationMessageSender stationMessageSender, long sendMeterValuesIntervalSec, long consumptionWattHour) {
        this.stationState = stationState;
        this.stationMessageSender = stationMessageSender;
        this.sendMeterValuesIntervalSec = sendMeterValuesIntervalSec;
        this.timeOfLastMeterValuePerEVSE = new HashMap<>();
        this.powerIncreasedPerInterval = sendMeterValuesIntervalSec * consumptionWattHour / 3600;
    }

    @Override
    public void run() {
        try {
            LocalDateTime now = LocalDateTime.now();
            StationState.StationStateView stationStateView = stationState.createView();
            for (Evse evse : stationState.getEvses()) {
                if (shouldSendMeterValue(now, evse.getId())) {
                    long powerUsed;
                    if (stationStateView.isCharging(evse.getId())) {
                        powerUsed = evse.incrementPowerConsumed(powerIncreasedPerInterval);
                    } else {
                        powerUsed = evse.getTotalConsumedWattHours();
                    }
                    stationMessageSender.sendTransactionEventUpdate(evse.getId(), null, TransactionEventRequest.TriggerReason.METER_VALUE_PERIODIC, TransactionData.ChargingState.CHARGING, powerUsed);
                    timeOfLastMeterValuePerEVSE.put(evse.getId(), now);
                }
            }
        } catch (Exception e) {
            log.error("Metervalues send exception", e);
        }
    }

    private boolean shouldSendMeterValue(LocalDateTime now, int evseId) {
        return sendMeterValuesIntervalSec > 0 && stationState.hasOngoingTransaction(evseId) && timeOfLastMeterValuePerEVSE.getOrDefault(evseId, LocalDateTime.MIN).plus(sendMeterValuesIntervalSec, ChronoUnit.SECONDS).isBefore(now);
    }
}
