package com.evbox.everon.ocpp.testutil.factory;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.simulator.message.MessageType;
import com.evbox.everon.ocpp.simulator.message.RawCall;
import com.google.common.collect.ImmutableList;
import io.undertow.Undertow;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.Value;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;
import static java.util.stream.Collectors.toList;

public class WebSocketServerMock {

    private final Undertow server;
    private final List<StationMessage> receivedMessages = new CopyOnWriteArrayList<>();
    private List<Expectation> expectations = new ArrayList<>();

    public WebSocketServerMock(String prefixPath, int port) {
        server = Undertow.builder().addHttpListener(port, "localhost").setHandler(path().addPrefixPath(prefixPath, websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                    final String messageData = message.getData();
                    String stationId = channel.getUrl().replace("ws://localhost:" + getPort() + "/ocpp/", "");
                    receivedMessages.add(new StationMessage(stationId, messageData));

                    if (RawCall.fromJson(messageData).getMessageType() == MessageType.CALL) {

                        Optional<Expectation> matchedExpectation = expectations.stream()
                                .filter(expectation -> expectation.matches(messageData)).findFirst();

                        if (matchedExpectation.isPresent()) {
                            String response = matchedExpectation.get().getReplier(messageData);
                            WebSockets.sendText(response, channel, null);
                        }
                    }
                }
            });
            channel.resumeReceives();
        }))).build();
    }

    public void start() {
        Executors.newSingleThreadExecutor().submit(server::start);
    }

    public int getPort() {
        int port = -1;
        while (port < 0) {
            try {
                List<Undertow.ListenerInfo> listeners = server.getListenerInfo();
                if (!listeners.isEmpty()) {
                    port = ((InetSocketAddress) listeners.get(0).getAddress()).getPort();
                }
            } catch (IllegalStateException e) {
                //ignore 'java.lang.IllegalStateException: UT000138: Server not started'
            }
        }
        return port;
    }

    public void addAnswer(Function<String, Boolean> matcher, Function<String, String> answer) {
        expectations.add(new Expectation(matcher, answer));
    }

    public void addCallAnswer(Function<Call, Boolean> matcher, Function<Call, String> answer) {
        addAnswer(
                request -> matcher.apply(Call.fromJson(request)),
                request -> answer.apply(Call.fromJson(request))
        );
    }

    public List<String> getReceivedMessages() {
        return ImmutableList.copyOf(receivedMessages.stream()
                .map(StationMessage::getMessage)
                .collect(toList()));
    }

    public List<String> getReceivedMessages(String stationId) {
        return ImmutableList.copyOf(receivedMessages.stream()
                .filter(stationMessage -> stationMessage.getStationId().equals(stationId))
                .map(StationMessage::getMessage)
                .collect(toList()));
    }

    public List<Call> getReceivedCalls(String stationId) {
        return getReceivedMessages(stationId).stream()
                .filter(msg -> RawCall.fromJson(msg).getMessageType() == MessageType.CALL)
                .map(Call::fromJson).collect(toList());
    }

    public List<CallResult> getReceivedCallResults(String stationId) {
        return getReceivedMessages(stationId).stream()
                .filter(msg -> RawCall.fromJson(msg).getMessageType() == MessageType.CALL_RESULT)
                .map(CallResult::from).collect(toList());
    }

    public List<Call> getReceivedCalls() {
        return getReceivedMessages().stream().map(Call::fromJson).collect(toList());
    }

    @Value
    static class Expectation {
        Function<String, Boolean> matcher;
        Function<String, String> replier;

        boolean matches(String request) {
            return matcher.apply(request);
        }

        String getReplier(String request) {
            return replier.apply(request);
        }
    }

    @Value
    static class StationMessage {
        String stationId;
        String message;
    }
}
