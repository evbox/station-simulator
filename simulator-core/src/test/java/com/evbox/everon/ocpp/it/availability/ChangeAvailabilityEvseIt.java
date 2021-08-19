package com.evbox.everon.ocpp.it.availability;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v201.message.station.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v201.message.station.EVSE;
import com.evbox.everon.ocpp.v201.message.station.OperationalStatus;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v201.message.station.ConnectorStatus.UNAVAILABLE;
import static com.evbox.everon.ocpp.v201.message.station.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v201.message.station.OperationalStatus.OPERATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class ChangeAvailabilityEvseIt extends StationSimulatorSetUp {

    @Test
    void shouldChangeEvseStatusToUnavailable() {

        ocppMockServer
                .when(StatusNotification.request(AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(UNAVAILABLE))
                .thenReturn(StatusNotification.response());

        // when
        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(INOPERATIVE));

        // then
        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            assertThat(station.getStateView().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.UNAVAILABLE);

            ocppMockServer.verify();
        });
    }

    @Test
    void shouldChangeEvseStatusToAvailable() {

        ocppMockServer
                .when(StatusNotification.request(AVAILABLE), times(2))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(UNAVAILABLE))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(INOPERATIVE));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(OPERATIVE));

        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            assertThat(station.getStateView().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.AVAILABLE);

            ocppMockServer.verify();
        });

    }

    String changeAvailabilityRequestWithStatus(OperationalStatus operationalStatus) {
        ChangeAvailabilityRequest changeAvailabilityRequest = new ChangeAvailabilityRequest()
                .withEvse(new EVSE().withId(DEFAULT_EVSE_ID))
                .withOperationalStatus(operationalStatus);

        return createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(CHANGE_AVAILABILITY_ACTION)
                .withPayload(changeAvailabilityRequest)
                .toJson();
    }
}
