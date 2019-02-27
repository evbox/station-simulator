package com.evbox.everon.ocpp.simulator.it;

import com.evbox.everon.ocpp.simulator.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.simulator.mock.ExpectedCount.twice;
import static com.evbox.everon.ocpp.simulator.mock.ExpectedRequests.bootNotificationRequest;
import static com.evbox.everon.ocpp.simulator.mock.ExpectedRequests.statusNotificationRequest;
import static com.evbox.everon.ocpp.simulator.mock.ExpectedResponses.bootNotificationResponse;
import static com.evbox.everon.ocpp.simulator.mock.ExpectedResponses.statusNotificationResponse;
import static com.evbox.everon.ocpp.simulator.support.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.simulator.support.StationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ChangeEvseAvailabilityIntegrationTest extends StationSimulatorSetUp {

    @Test
    void shouldChangeEvseStatusToUnavailable() {

        // given
        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponse());

        ocppMockServer
                .when(statusNotificationRequest(), twice())
                .thenReturn(statusNotificationResponse());

        // when
        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(createChangeAvailabilityRequest());

        // then
        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            assertThat(station.getState().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.UNAVAILABLE);

            ocppMockServer.verify();
        });



    }

    String createChangeAvailabilityRequest() {
        ChangeAvailabilityRequest changeAvailabilityRequest = new ChangeAvailabilityRequest()
                .withEvseId(DEFAULT_EVSE_ID)
                .withOperationalStatus(ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE);

        return createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(CHANGE_AVAILABILITY_ACTION)
                .withPayload(changeAvailabilityRequest)
                .toJson();
    }
}
