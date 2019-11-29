package com.evbox.everon.ocpp.simulator.station.actions.user;

import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents switch to security profile 3 message.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class SecurityProfile3 implements UserMessage {

    private String url;

    @Override
    public void perform(StateManager stateManager) {
        stateManager.getStation().switchToSecurityProfile3(url);
    }

}
