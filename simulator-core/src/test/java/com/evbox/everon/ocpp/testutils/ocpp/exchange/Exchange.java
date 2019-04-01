package com.evbox.everon.ocpp.testutils.ocpp.exchange;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.testutils.factory.JsonMessageTypeFactory;

import java.util.function.Function;

/**
 * Class for common responses and requests.
 */
public abstract class Exchange {

    /**
     * Create a response with empty payload.
     *
     * @return response in json.
     */
    public static Function<Call, String> defaultResponse() {
        return request -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(request.getMessageId())
                .withPayload("")
                .toJson();
    }

    protected static boolean equalsType(Call request, ActionType type) {
        return request.getActionType() == type;
    }
}
