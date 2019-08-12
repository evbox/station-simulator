package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.simulator.station.actions.Plug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.common.IdToken.Type.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SendingTransactionRelatedMeterValues extends StationSimulatorSetUp {

    private final long METER_VALUES_INTERVAL = 1_000;
    private final long POWER_CONSUMPTION_PER_INTERVAL = 100;

    @Override
    protected long getMeterValuesInterval() {
        return METER_VALUES_INTERVAL;
    }

    @Override
    protected long getPowerConsumptionPerInterval() {
        return POWER_CONSUMPTION_PER_INTERVAL;
    }

    @Test
    void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), times(2))
                .thenReturn(Authorize.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue();
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getPowerConsumed()).isGreaterThanOrEqualTo(POWER_CONSUMPTION_PER_INTERVAL);
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getPowerConsumed()).isGreaterThanOrEqualTo(2 * POWER_CONSUMPTION_PER_INTERVAL);
        });

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse();

            // The transaction is still going on but the evse is not charging, so the power consumed should not increase
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getPowerConsumed()).isLessThan(3 * POWER_CONSUMPTION_PER_INTERVAL);
            ocppMockServer.verify();
        });

    }
}