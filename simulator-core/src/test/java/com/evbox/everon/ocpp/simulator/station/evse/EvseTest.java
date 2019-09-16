package com.evbox.everon.ocpp.simulator.station.evse;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

class EvseTest {

    private final int CONNECTORS_PER_EVSE = 2;
    private final int EVSE_ID = 1;

    private Evse evse;

    @BeforeEach
    void setUp() {

        ImmutableList.Builder<Connector> connectorListBuilder = ImmutableList.builder();
        for (int connectorId = 1; connectorId <= CONNECTORS_PER_EVSE; connectorId++) {
            connectorListBuilder.add(new Connector(connectorId, CableStatus.UNPLUGGED, AVAILABLE));
        }

        evse = new Evse(EVSE_ID, connectorListBuilder.build());
    }

    @Test
    void testPowerIncrement() {
        long startingPowerConsumed = evse.getTotalConsumedWattHours();
        assertThat(startingPowerConsumed).isEqualTo(0);

        long incrementValue = 1000;
        for (int i = 1; i < 20; i++) {
            long incrementedValue = evse.incrementPowerConsumed(incrementValue);
            assertThat(incrementedValue).isEqualTo(incrementValue * i);
            assertThat(evse.getTotalConsumedWattHours()).isEqualTo(incrementValue * i);
        }
    }

    @Test
    void testPowerIncrementOverflow() {
        long startingPowerConsumed = evse.getTotalConsumedWattHours();
        assertThat(startingPowerConsumed).isEqualTo(0);

        long incrementValue = Long.MAX_VALUE - 150;
        assertThat(evse.incrementPowerConsumed(incrementValue)).isEqualTo(incrementValue);
        assertThat(evse.incrementPowerConsumed(100)).isEqualTo(incrementValue + 100);

        // The counter starts from zero and zero is counted as a value, that's why it is 49 and not 50
        assertThat(evse.incrementPowerConsumed(100)).isEqualTo(49);
    }

    @Test
    void testPowerIncrementRollToZero() {
        long startingPowerConsumed = evse.getTotalConsumedWattHours();
        assertThat(startingPowerConsumed).isEqualTo(0);

        long startingValue = Long.MAX_VALUE - 10;
        assertThat(evse.incrementPowerConsumed(startingValue)).isEqualTo(startingValue);

        for (int i = 1; i <= 10; i++) {
            assertThat(evse.incrementPowerConsumed(1)).isEqualTo(startingValue + i);
        }

        assertThat(evse.getTotalConsumedWattHours()).isEqualTo(Long.MAX_VALUE);

        for (int i = 0; i <= 10; i++) {
            assertThat(evse.incrementPowerConsumed(1)).isEqualTo(i);
        }
    }

    @Test
    void testFindAvailableConnector() {
        assertThat(evse.tryFindAvailableConnector().map(Connector::getId).orElse(0)).isEqualTo(1);
        evse.findConnector(1).plug();

        assertThat(evse.tryFindAvailableConnector().map(Connector::getId).orElse(0)).isEqualTo(2);
        evse.findConnector(2).plug();

        assertThat(evse.tryFindAvailableConnector()).isEqualTo(Optional.empty());
    }

}
