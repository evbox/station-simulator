package com.evbox.everon.ocpp.simulator.station.subscription;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporarily subscription storage.
 */
public class SubscriptionRegistry {

    private final Map<String, SubscriptionContext> responseSubscriptions = new HashMap<>();

    /**
     * Add a subscription to the storage.
     *
     * @param callId key in the storage
     * @param payload body of the request
     * @param subscriber callback associated with the callId
     * @param <REQ> server request
     * @param <RES> server response
     */
    public <REQ, RES> void addSubscription(String callId, REQ payload, Subscriber<REQ, RES> subscriber) {

        responseSubscriptions.put(callId, new SubscriptionContext<>(subscriber, payload));

    }

    /**
     * Find and execute callback associated with the callId.
     *
     * @param callId key in the storage
     * @param response server response
     */
    public void fulfillSubscription(String callId, Object response) {

        if (responseSubscriptions.containsKey(callId)) {
            SubscriptionContext subscriptionContext = responseSubscriptions.get(callId);
            subscriptionContext.getSubscriber().onResponse(subscriptionContext.getRequest(), response);
            responseSubscriptions.remove(callId);
        }

    }


}
