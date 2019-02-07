package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.RawCall;
import com.evbox.everon.ocpp.simulator.user.interaction.UserAction;
import com.evbox.everon.ocpp.simulator.websocket.WebSocketClientInboxMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.evbox.everon.ocpp.simulator.station.Evse.ConnectorState.LOCKED;
import static com.evbox.everon.ocpp.simulator.station.Evse.ConnectorState.OCCUPIED;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StationTest {
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private BlockingQueue<WebSocketClientInboxMessage> webSocketInbox = new LinkedBlockingQueue<>();

    private Station station;

    @Before
    public void setUp() {
        station = new Station(Runnable::run, new LinkedBlockingQueue<>(), webSocketInbox, new StationConfiguration("EVB-P18090564", 1, 1, DEFAULT_HEARTBEAT_INTERVAL));
    }

    @Test
    public void shouldIncreaseMessageIdForConsequentCalls() {
        //when
        triggerUserAction(new UserAction.Plug(1));
        triggerUserAction(new UserAction.Unplug(1));
        triggerUserAction(new UserAction.Plug(1));

        //then
        List<String> sentCalls =
                webSocketInbox.stream().filter(message -> message.getType() == WebSocketClientInboxMessage.Type.OCPP_MESSAGE)
                        .flatMap(message -> message.getData().map(Stream::of).orElse(Stream.empty())).map(messageObj -> (String) messageObj).collect(toList());

        assertThat(sentCalls).hasSize(3);
        List<String> messageIds = sentCalls.stream()
                .map(Call::fromJson)
                .map(Call::getMessageId)
                .collect(toList());

        assertThat(messageIds).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    public void shouldNotAllowToPlugIntoOccupiedConnector() {
        //given
        int connectorId = 1;
        triggerUserAction(new UserAction.Plug(connectorId));

        //when
        triggerUserAction(new UserAction.Plug(connectorId));

        //then
        List<String> statusNotificationRequests = webSocketInbox.stream()
                .map(message -> (String) message.getData().orElseThrow(() -> new RuntimeException("Payload should not be empty")))
                .filter(message -> message.contains(ActionType.STATUS_NOTIFICATION.getType()))
                .collect(toList());

        assertThat(statusNotificationRequests).hasSize(1);
    }

    @Test
    public void shouldStartTransactionWhenCablePluggedAfterAuthorization() {
        //given
        triggerUserAction(new UserAction.Authorize("04E8960A1A3180", 1));

        mockServerResponse((request) -> "[3, \""+request.getMessageId()+"\", {\"idTokenInfo\":{\"status\":\"Accepted\"}}]"); //authorization response
        mockServerResponse((request) -> "[3, \""+request.getMessageId()+"\", {}]"); //transaction started response

        //when
        triggerUserAction(new UserAction.Plug(1));

        mockServerResponse((request) -> "[3, \""+request.getMessageId()+"\", {}]"); //status notification response
        mockServerResponse((request) -> "[3, \""+request.getMessageId()+"\", {}]"); //transaction updated response

        //then
        assertThat(station.getState().getConnectorState(1)).isEqualTo(LOCKED);
        assertThat(station.getState().isCharging(1)).isEqualTo(true);
    }

    @Test
    public void shouldChangeConnectorStateToOccupied() {
        //when
        triggerUserAction(new UserAction.Plug(1));

        //then
        assertThat(station.getState().getConnectorState(1)).isEqualTo(OCCUPIED);
        assertThat(station.getState().isCharging(1)).isEqualTo(false);
    }

    private void mockServerResponse(Function<RawCall, String> replyFunction) {
        String serverResponse = replyFunction.apply(RawCall.fromJson((String) webSocketInbox.poll().getData().orElseThrow(() -> new RuntimeException("Unexpected error"))));
        station.getInbox().add(new StationInboxMessage(StationInboxMessage.Type.OCPP_MESSAGE, serverResponse));
        station.processMessage();
    }

    private void triggerUserAction(UserAction userAction) {
        station.getInbox().add(new StationInboxMessage(StationInboxMessage.Type.USER_ACTION, userAction));
        station.processMessage();
    }
}