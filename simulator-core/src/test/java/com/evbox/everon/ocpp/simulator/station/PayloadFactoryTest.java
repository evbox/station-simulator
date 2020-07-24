package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.v20.message.centralserver.DataTransferRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.evbox.everon.ocpp.simulator.station.PayloadFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

class PayloadFactoryTest {

    PayloadFactory payloadFactory = new PayloadFactory();

    @Test
    void shouldBuildDataTransferRequest() {
        List<Integer> connectorIds = List.of(1, 2, 3);
        DataTransferRequest request = payloadFactory.createPublicKeyDataTransfer(connectorIds);
        assertThat(request.getVendorId()).isEqualTo(GENERAL_CONFIGURATION);
        assertThat(request.getMessageId()).isEqualTo(SET_METER_CONFIGURATION);
        assertThat(String.valueOf(request.getData())).contains(PUBLIC_KEY);
    }
}