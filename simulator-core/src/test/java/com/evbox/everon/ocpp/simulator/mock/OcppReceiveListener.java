package com.evbox.everon.ocpp.simulator.mock;

import com.evbox.everon.ocpp.simulator.message.MessageType;
import com.evbox.everon.ocpp.simulator.message.RawCall;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * A receive listener that handles an incoming text message.
 */
@Slf4j
@AllArgsConstructor
public class OcppReceiveListener extends AbstractReceiveListener {

    private final RequestExpectationManager requestExpectationManager;

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        final String incomingRequest = message.getData();
        log.debug("Incoming request: {}", incomingRequest);

        if (RawCall.fromJson(incomingRequest).getMessageType() == MessageType.CALL) {
            Optional<String> expectedResponse = requestExpectationManager.findExpectedResponse(incomingRequest);
            if (expectedResponse.isPresent()) {
                log.debug("Expectation is found.");
                WebSockets.sendText(expectedResponse.get(), channel, null);
            } else {
                requestExpectationManager.addUnexpectedRequest(incomingRequest);
            }
        } else {
            log.debug("Incoming request is not of type Call");
        }

    }
}
