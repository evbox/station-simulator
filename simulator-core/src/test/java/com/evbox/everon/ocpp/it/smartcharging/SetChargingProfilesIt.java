package com.evbox.everon.ocpp.it.smartcharging;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.station.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.v201.message.station.IdTokenType.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;

public class SetChargingProfilesIt extends StationSimulatorSetUp {

    @Test
    void shouldRejectSetChargingProfileRequests() {
        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        ChargingProfile chargingProfile = new ChargingProfile()
                                                .withId(1)
                                                .withStackLevel(2)
                                                .withChargingProfilePurpose(ChargingProfilePurpose.TX_DEFAULT_PROFILE)
                                                .withChargingProfileKind(ChargingProfileKind.ABSOLUTE)
                                                .withChargingSchedule(Collections.singletonList(new ChargingSchedule().withChargingRateUnit(ChargingRateUnit.A)));
        SetChargingProfileRequest chargingProfileRequest = new SetChargingProfileRequest()
                                                                .withEvseId(1)
                                                                .withChargingProfile(chargingProfile);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_CHARGING_PROFILE, chargingProfileRequest);
        SetChargingProfileResponse response = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), SetChargingProfileResponse.class);
        assertThat(response.getStatus()).isEqualTo(ChargingProfileStatus.REJECTED);
    }
}
