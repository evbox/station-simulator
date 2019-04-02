package com.evbox.everon.ocpp.mock.ocpp.exchange;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;

import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.ocpp.exchange.Common.equalsType;

public class Heartbeat {

    /**
     * HeartbeatRequest with any configuration.
     *
     * @return checks whether an incoming request is HeartbeatRequest or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, ActionType.HEARTBEAT);
    }

    /**
     * Create a HeartbeatResponse with given timestamp.
     *
     * @param serverTime time of the server
     * @return response in json.
     */
    public static Function<Call, String> response(ZonedDateTime serverTime) {
        return incomingRequest -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(incomingRequest.getMessageId())
                .withCurrentTime(serverTime.toString())
                .toJson();
    }
}
