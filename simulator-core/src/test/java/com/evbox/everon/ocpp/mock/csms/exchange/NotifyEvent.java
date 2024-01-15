package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v201.message.station.EventData;
import com.evbox.everon.ocpp.v201.message.station.NotifyEventRequest;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.emptyResponse;
import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.NOTIFY_EVENT;

public class NotifyEvent {
    /**
     * NotifyEventRequest with any configuration
     *
     * @return checks whether an incoming request is NotifyEvent or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, NOTIFY_EVENT);
    }

    /**
     * NotifyEventRequest that should have the expected data.
     *
     * @param evseId evse id
     * @param connectorId connector id
     * @param errorCode error code
     * @param errorDescription error description
     * @return checks whether an incoming request is a matching NotifyEvent.
     */
    public static Predicate<Call> request(int evseId, int connectorId, String errorCode, String errorDescription) {
        return request -> equalsType(request, NOTIFY_EVENT) && hasEventDataWith((NotifyEventRequest) request.getPayload(), eventData(evseId, connectorId, errorCode, errorDescription));
    }

    /**
     * Create a response for NotifyEvent.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return emptyResponse();
    }

    /**
     * NotifyEventRequest that should have at least 1 eventData that satisfies the given predicate.
     * @param request the request to check
     * @param predicate predicate to check
     * @return true if the request has at least 1 eventData that satisfies the given predicate
     */
    private static boolean hasEventDataWith(NotifyEventRequest request, Predicate<EventData> predicate) {
        return request.getEventData().stream().anyMatch(predicate);
    }

    /**
     * Predicate that checks if the given eventData has the expected values.
     * @param evseId evse id
     * @param connectorId connector id
     * @param errorCode error code
     * @param errorDescription error description
     * @return predicate that checks if the given eventData has the expected values
     */
    private static Predicate<EventData> eventData(int evseId, int connectorId, String errorCode, String errorDescription) {
        return eventData -> hasEvseId(eventData, evseId) && hasConnectorId(eventData, connectorId) && hasErrorCode(eventData, errorCode) && hasErrorDescription(eventData, errorDescription);
    }

    /**
     * Checks if the given eventData has the expected evseId in the evse of the component.
     * @param eventData the eventData to check
     * @param evseId the expected evseId
     * @return true if the given eventData has the expected evseId
     */
    private static boolean hasEvseId(EventData eventData, int evseId) {
        return eventData.getComponent().getEvse().getId() == evseId;
    }

    /**
     * Checks if the given eventData has the expected connectorId in the evse of the component.
     * @param eventData the eventData to check
     * @param connectorId the expected connectorId
     * @return true if the given eventData has the expected connectorId
     */
    private static boolean hasConnectorId(EventData eventData, int connectorId) {
        return eventData.getComponent().getEvse().getConnectorId() == connectorId;
    }

    /**
     * Checks if the given eventData has the expected errorCode in the tech code.
     * @param eventData the eventData to check
     * @param errorCode the expected errorCode
     * @return true if the given eventData has the expected errorCode
     */
    private static boolean hasErrorCode(EventData eventData, String errorCode) {
        return eventData.getTechCode().toString().equals(errorCode);
    }

    /**
     * Checks if the given eventData has the expected errorDescription in the tech info.
     * @param eventData the eventData to check
     * @param errorDescription the expected errorDescription
     * @return true if the given eventData has the expected errorDescription
     */
    private static boolean hasErrorDescription(EventData eventData, String errorDescription) {
        return eventData.getTechInfo().toString().equals(errorDescription);
    }
}
