package com.evbox.everon.ocpp.simulator.websocket;

import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public class OkHttpWebSocketClient {

    private static final String COLON = ":";

    private final OkHttpClient client;
    private final SimulatorConfiguration.StationConfiguration stationConfiguration;
    private WebSocket webSocket;
    private ChannelListener listener;

    public OkHttpWebSocketClient(OkHttpClient client, SimulatorConfiguration.StationConfiguration stationConfiguration) {
        this.client = client;
        this.stationConfiguration = stationConfiguration;
    }

    public void setListener(ChannelListener listener) {
        this.listener = listener;
    }

    public void connect(String url) {

        Request.Builder requestBuilder = new Request.Builder().url(url)
                .addHeader("Sec-WebSocket-Protocol", "ocpp2.0");

        if (nonNull(stationConfiguration.getBasicAuthPassword())) {

            try {
                byte[] plainCredentials = prepareAuthPassword();
                requestBuilder.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(plainCredentials));
                System.out.println("@@@@@@@@@ Sending from " + Thread.currentThread().getId() + " pass " + stationConfiguration.getBasicAuthPassword());
            } catch (DecoderException e) {
                log.error(e.getMessage(), e);
            }

        }

        webSocket = client.newWebSocket(requestBuilder.build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                listener.onOpen(response.toString());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                listener.onMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                listener.onMessage(new String(bytes.toByteArray()));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                listener.onClosing(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                listener.onClosing(code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                listener.onFailure(t, Optional.ofNullable(response).map(Response::toString).orElse(""));
            }
        });
    }

    public void disconnect() {
        webSocket.close(1000, "Simulator goes offline");
    }

    public void reconnect(String url) {
        log.info("Trying to reconnect to {}", url);
        disconnect();
        connect(url);
    }

    public boolean sendMessage(String message) {
        return webSocket.send(message);
    }

    private byte[] prepareAuthPassword() throws DecoderException {

        byte[] decodedPassword = Hex.decodeHex(stationConfiguration.getBasicAuthPassword());

        byte[] username = stationConfiguration.getId().getBytes();

        int size = username.length + decodedPassword.length + 1;

        return ByteBuffer.allocate(size)
                .put(username)
                .put(COLON.getBytes())
                .put(decodedPassword)
                .array();
    }

}
