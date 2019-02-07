package com.evbox.everon.ocpp.simulator.message;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RawCallTest {

    @Test
    public void shouldParseCallResult() {
        //given
        String json = "[3,\"19223201\", { \"currentTime\": \"2013-02-01T20:53:32.486Z\", \"interval\": 300, \"status\": \"Accepted\"} ]";

        //when
        RawCall rawCall = RawCall.fromJson(json);

        //then
        assertThat(rawCall.getMessageId()).isEqualTo("19223201");
        assertThat(rawCall.getMessageType()).isEqualTo(MessageType.CALL_RESULT);
        assertThat(rawCall.getPayload()).isNotNull();
    }

    @Test
    public void shouldParseErrorCall() {
        //given
        String json = "[4,\"19223201\",\"FormationViolation\",\"Could not parse incoming call\",{}]";

        //when
        RawCall rawCall = RawCall.fromJson(json);

        //then
        assertThat(rawCall.getMessageId()).isEqualTo("19223201");
        assertThat(rawCall.getMessageType()).isEqualTo(MessageType.CALL_ERROR);
    }

    @Test
    public void shouldThrowExceptionOnUnknownMessageType() {
        //given
        String unknownId = "unknownId";
        String json = "[\"" + unknownId + "\",\"19223201\", { \"currentTime\": \"2013-02-01T20:53:32.486Z\", \"interval\": 300, \"status\": \"Accepted\"} ]";

        //when
        RawCall rawCall = RawCall.fromJson(json);

        //then
        assertThatThrownBy(rawCall::getPayload).hasMessage("Unable to parse message type: unknownId");
    }

    @Test
    public void shouldThrowExceptionOnFieldValueTypeMismatch() {
        //given
        String json = "[3,19223201, { \"currentTime\": \"2013-02-01T20:53:32.486Z\", \"interval\": 300, \"status\": \"Accepted\"} ]";

        //when
        RawCall rawCall = RawCall.fromJson(json);

        //then
        assertThatThrownBy(rawCall::getMessageId).hasMessage("Field value type 'java.lang.Integer' does not correspond to required 'java.lang.String'");
    }
}