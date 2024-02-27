package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.ClearDisplayMessageRequest;
import com.evbox.everon.ocpp.v201.message.station.ClearDisplayMessageResponse;
import com.evbox.everon.ocpp.v201.message.station.ClearMessageStatus;
import com.evbox.everon.ocpp.v201.message.station.NotifyDisplayMessagesRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DisplayMessageHandler {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperHolder.JSON_OBJECT_MAPPER;

    public static OcppRequestHandler<SetDisplayMessageRequest> createSetDisplayMessageHandler(StationMessageSender stationMessageSender, StationStore stationStore) {
        return (String callId, SetDisplayMessageRequest request) -> {
            MessageInfo message = request.getMessage();

            stationStore.addDisplayMessage(message);

            stationMessageSender.sendCallResult(callId, new SetDisplayMessageResponse()
                    .withStatus(DisplayMessageStatus.ACCEPTED)
                    .withStatusInfo(new StatusInfo().withReasonCode(new CiString.CiString20("Message accepted"))));
        };
    }

    public static OcppRequestHandler<GetDisplayMessagesRequest> createGetDisplayMessage(StationMessageSender stationMessageSender, StationStore stationStore) {
        return (String callId, GetDisplayMessagesRequest request) -> {
            Integer requestId = request.getRequestId();
            boolean isGetAllRequest = (request.getId() == null || request.getId().isEmpty()) &&
                    request.getPriority() == null && request.getState() == null;

            Predicate<MessageInfo> messageFilter = (MessageInfo message) -> Optional.ofNullable(request.getPriority()).map(filter -> filter.equals(message.getPriority())).orElse(true) &&
                    Optional.ofNullable(request.getState()).map(filter -> filter.equals(message.getState())).orElse(true) &&
                    Optional.ofNullable(request.getId()).map(filter -> filter.contains(message.getId())).orElse(true);

            Stream<MessageInfo> filteredMessages = isGetAllRequest
                    ? stationStore.getAllDisplayMessage().stream()
                    : stationStore.getAllDisplayMessage().stream().filter(messageFilter);

            var messagesToReturn = filteredMessages.map(DisplayMessageHandler::mapViaJson).collect(Collectors.toList());
            var response = new GetDisplayMessagesResponse()
                    .withStatus(messagesToReturn.isEmpty() ? GetDisplayMessagesStatus.UNKNOWN : GetDisplayMessagesStatus.ACCEPTED);
            stationMessageSender.sendCallResult(callId, response);

            if (!messagesToReturn.isEmpty()) {
                stationMessageSender.sendDisplayMessage(new NotifyDisplayMessagesRequest()
                        .withRequestId(requestId)
                        .withMessageInfo(messagesToReturn));
            }
        };
    }

    private static com.evbox.everon.ocpp.v201.message.station.MessageInfo mapViaJson(MessageInfo x) {
        try {
            String s = OBJECT_MAPPER.writeValueAsString(x);
            return OBJECT_MAPPER.readValue(s, com.evbox.everon.ocpp.v201.message.station.MessageInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static OcppRequestHandler<ClearDisplayMessageRequest> createClearDisplayMessage(StationMessageSender stationMessageSender, StationStore stationStore) {
        return (String callId, ClearDisplayMessageRequest request) -> {
            Integer messageId = request.getId();
            boolean removed = stationStore.removeDisplayMessage(messageId);

            var response = new ClearDisplayMessageResponse()
                    .withStatus(removed ? ClearMessageStatus.ACCEPTED : ClearMessageStatus.UNKNOWN);
            stationMessageSender.sendCallResult(callId, response);
        };
    }
}
