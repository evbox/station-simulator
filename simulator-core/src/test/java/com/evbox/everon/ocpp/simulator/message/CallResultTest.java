package com.evbox.everon.ocpp.simulator.message;

import com.evbox.everon.ocpp.v201.message.station.HeartbeatResponse;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class CallResultTest {

    @Test
    void shouldParsePayloadToSpecificType() {
        //given
        String callResultJson = "[3,\n" + " \"19223201\",\n" +
                " {\n" +
                "   \"currentTime\": \"2013-02-01T20:53:32.0Z\"\n" +
                "} ]";
        CallResult callResult = CallResult.from(callResultJson);

        //when
        HeartbeatResponse heartbeatPayload = callResult.getPayload(HeartbeatResponse.class);

        //then
        assertThat(heartbeatPayload.getCurrentTime()).isEqualTo(ZonedDateTime.of(2013, 2, 1, 20, 53, 32, 0, ZoneOffset.UTC));
    }
}
