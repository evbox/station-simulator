package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.centralserver.DisplayMessageStatus;
import com.evbox.everon.ocpp.v201.message.centralserver.SetDisplayMessageRequest;
import com.evbox.everon.ocpp.v201.message.centralserver.SetDisplayMessageResponse;
import com.evbox.everon.ocpp.v201.message.centralserver.StatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SetDisplayMessageHandler implements OcppRequestHandler<SetDisplayMessageRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationStore stationStore;

    @Override
    public void handle(String callId, SetDisplayMessageRequest request) {
        Integer id = request.getMessage().getId();
        String content = request.getMessage().getMessage().getContent().toString();
        stationStore.addDisplayMessage(id, content);
        stationMessageSender.sendCallResult(callId, new SetDisplayMessageResponse()
                .withStatus(DisplayMessageStatus.ACCEPTED)
                .withStatusInfo(new StatusInfo().withReasonCode(new CiString.CiString20("Message accepted"))));
    }
}
