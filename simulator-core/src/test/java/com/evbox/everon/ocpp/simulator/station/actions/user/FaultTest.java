package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FaultTest {
    @Mock
    StateManager stateManager;

    @Test
    void verifyPerform() {
        // given
        Fault fault = new Fault(1, 1, "errorCode", "errorDescription");

        // when
        fault.perform(stateManager);

        // then
        verify(stateManager).faulted(1, 1, "errorCode", "errorDescription");
    }
}
