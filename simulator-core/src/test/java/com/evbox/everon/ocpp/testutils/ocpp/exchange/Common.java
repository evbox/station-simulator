package com.evbox.everon.ocpp.testutils.ocpp.exchange;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutils.factory.JsonMessageTypeFactory;

import java.util.function.Function;

public final class Common {

    /**
     * Returns an empty response.
     * @return empty response in json.
     */
    public static Function<Call, String> emptyResponse() {
        return request -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(request.getMessageId())
                .withPayload("")
                .toJson();
    }

    protected static boolean equalsType(Call request, ActionType type) {
        return request.getActionType() == type;
    }
}
