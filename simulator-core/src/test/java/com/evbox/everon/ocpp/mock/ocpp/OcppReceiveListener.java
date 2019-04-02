package com.evbox.everon.ocpp.mock.ocpp;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.message.RawCall;
import com.evbox.everon.ocpp.mock.expect.RequestExpectationManager;
import com.evbox.everon.ocpp.mock.expect.ResponseExpectationManager;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;

import static com.evbox.everon.ocpp.mock.ocpp.exchange.Common.emptyResponse;

/**
 * A receive listener that handles an incoming text message.
 */
@Slf4j
@AllArgsConstructor
public class OcppReceiveListener extends AbstractReceiveListener {

    private final RequestExpectationManager requestExpectationManager;
    private final ResponseExpectationManager responseExpectationManager;
    private final OcppServerClient ocppServerClient;

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        final String request = message.getData();
        log.debug("OCPP incoming request: {}", request);

        switch (RawCall.fromJson(request).getMessageType()) {
            case CALL_RESULT:
                handleCallResult(request);
                break;
            case CALL:
                handleCall(channel, request);
                break;
            default:
                log.debug("Incoming request is not of type Call or CallResult");
        }
    }

    private void handleCall(WebSocketChannel channel, String request) {
        Call incomingCall = Call.fromJson(request);

        Optional<Function<Call, String>> expectedResponse = requestExpectationManager.findExpectedResponse(incomingCall);
        if (expectedResponse.isPresent()) {
            String responseToBeSend = expectedResponse.get().apply(incomingCall);
            log.debug("Expectation is found. Sending response: {}", responseToBeSend);
            WebSockets.sendText(responseToBeSend, channel, null);

            // the first request should always be BootNotification
            ocppServerClient.setConnected(true);
        } else {
            WebSockets.sendText(emptyResponse().apply(incomingCall), channel, null);
            requestExpectationManager.addUnexpectedCall(request);
        }
    }

    private void handleCallResult(String request) {
        CallResult response = CallResult.from(request);
        if (!responseExpectationManager.isExpectedResponsePresent(response)) {
            responseExpectationManager.addUnexpectedCall(request);
        }
    }
}
