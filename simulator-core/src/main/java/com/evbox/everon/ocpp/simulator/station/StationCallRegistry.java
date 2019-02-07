package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains logic for handling subscriptions for call results.
 */
@FieldDefaults(makeFinal = true)
public class StationCallRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationCallRegistry.class);

    //This is a simplified implementation. According to docs MessageID can encounter up to 36 characters (e.g. satisfy UUID string).
    private AtomicInteger callIdSequence = new AtomicInteger(1);

    private Map<String, SubscriptionContext> responseSubscriptions = new ConcurrentHashMap<>();
    private Map<String, Call> sentCalls = new ConcurrentHashMap<>();

    private BlockingQueue<WebSocketClientInboxMessage> webSocketInbox;

    public StationCallRegistry(BlockingQueue<WebSocketClientInboxMessage> webSocketInbox) {
        this.webSocketInbox = webSocketInbox;
    }

    public <REQ, RES> void subscribeAndSend(ActionType actionType, REQ payload, List<Subscriber<REQ, RES>> subscribers) {
        String callId = nextCallId();

        subscribers.forEach(subscriber -> responseSubscriptions.put(callId, new SubscriptionContext<>(subscriber, payload)));
        sendRequest(callId, actionType, payload);
    }

    public void fulfillSubscription(String callId, Object response) {
        Optional.ofNullable(responseSubscriptions.get(callId))
                .ifPresent(context -> context.getSubscriber().onResponse(context.getRequest(), response));
        responseSubscriptions.remove(callId);
    }

    public void sendRequest(ActionType actionType, Object payload) {
        String callId = nextCallId();
        Call call = new Call(callId, actionType, payload);

        saveCall(callId, call);

        String callStr = call.toJson();
        sendCall(callStr);
    }

    public Map<String, Call> getSentCalls() {
        return sentCalls;
    }

    private Call saveCall(String callId, Call call) {
        return sentCalls.put(callId, call);
    }

    private void sendRequest(String callId, ActionType actionType, Object payload) {
        Call call = new Call(callId, actionType, payload);
        saveCall(callId, call);
        sendCall(call.toJson());
    }

    private void sendCall(String call) {
        try {
            webSocketInbox.put(new WebSocketClientInboxMessage.OcppMessage(call));
        } catch (InterruptedException e) {
            LOGGER.error("Exception on adding message to WebSocketInbox", e);
            Thread.currentThread().interrupt();
        }
    }

    private String nextCallId() {
        return String.valueOf(callIdSequence.getAndIncrement());
    }
}
