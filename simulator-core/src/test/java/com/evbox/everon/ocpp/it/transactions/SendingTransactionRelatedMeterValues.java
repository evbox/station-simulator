package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SendingTransactionRelatedMeterValues extends StationSimulatorSetUp {

    private final long DEFAULT_SEND_METER_VALUES_INTERVAL_SEC = 1;
    private final long DEFAULT_CONSUMPTION_WATT_HOUR = 3600;

    @Override
    protected SimulatorConfiguration.MeterValuesConfiguration getMeterValuesConfiguration() {
        return SimulatorConfiguration.MeterValuesConfiguration.builder()
                                                                .sendMeterValuesIntervalSec(DEFAULT_SEND_METER_VALUES_INTERVAL_SEC)
                                                                .consumptionWattHour(DEFAULT_CONSUMPTION_WATT_HOUR)
                                                                .build();
    }

    @Test
    void shoudlReceiveMeterValuesDuringTransaction() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), times(2))
                .thenReturn(Authorize.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue();
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTotalConsumedWattHours()).isGreaterThanOrEqualTo(DEFAULT_CONSUMPTION_WATT_HOUR / 3600);
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTotalConsumedWattHours()).isGreaterThanOrEqualTo(2 * DEFAULT_CONSUMPTION_WATT_HOUR / 3600);
        });

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse();

            // The transaction is still going on but the evse is not charging, so the power consumed should not increase
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTotalConsumedWattHours()).isLessThan(3 * DEFAULT_CONSUMPTION_WATT_HOUR / 3600);
            ocppMockServer.verify();
        });
    }
}