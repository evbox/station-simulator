package com.evbox.everon.ocpp.mock.csms;

import com.evbox.everon.ocpp.mock.expect.ExpectedCount;
import com.evbox.everon.ocpp.mock.expect.RequestExpectationManager;
import com.evbox.everon.ocpp.simulator.message.Call;
import io.undertow.Undertow;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.once;
import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * WebSocket Ocpp mock server.
 */
@Slf4j
public class OcppMockServer {

    private Undertow server;

    private final RequestExpectationManager requestExpectationManager = new RequestExpectationManager();
    private final RequestResponseSynchronizer requestResponseSynchronizer = new RequestResponseSynchronizer();

    private final AtomicInteger connectionAttempts = new AtomicInteger();

    private final OcppServerClient ocppServerClient;
    private final String hostname;
    private final int port;
    private final String path;
    private final OcppIdentityManager identityManager;

    private OcppMockServer(OcppServerMockBuilder builder) {
        Objects.requireNonNull(builder.hostname);
        Objects.requireNonNull(builder.path);
        Objects.requireNonNull(builder.ocppServerClient);

        this.ocppServerClient = builder.ocppServerClient;
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.path = builder.path;
        this.identityManager = new OcppIdentityManager(builder.username, builder.password);
    }

    /**
     * Start mock server.
     */
    public void start() {
        String targetUrl = "ws://" + hostname + ":" + port + path + "/";

        server = Undertow.builder()
                .addHttpListener(port, hostname)
                .setHandler(
                        path().addPrefixPath(path, authentication(websocket((exchange, channel) -> {
                            connectionAttempts.incrementAndGet();
                            String stationId = channel.getUrl().replace(targetUrl, "");
                            channel.getReceiveSetter().set(new OcppReceiveListener(requestExpectationManager, ocppServerClient, requestResponseSynchronizer));
                            channel.resumeReceives();

                            ocppServerClient.putIfAbsent(stationId, new WebSocketSender(channel, requestResponseSynchronizer));

                        }))))
                .build();

        server.start();
    }

    /**
     * Stops ocpp mock server.
     */
    public void stop() {
        server.stop();
    }

    /**
     * Accepts a predicate that is responsible for incoming request expectation.
     * By default: expected count is 1.
     *
     * @param requestExpectation a request expectation predicate
     * @return {@link OcppServerResponse} instance
     */
    public OcppServerResponse when(Predicate<Call> requestExpectation) {
        return when(requestExpectation, once());
    }

    /**
     * Accepts a predicate that is responsible for incoming request expectation.
     *
     * @param requestExpectation a request expectation predicate
     * @param expectedCount      expected count
     * @return {@link OcppServerResponse} instance
     */
    public OcppServerResponse when(Predicate<Call> requestExpectation, ExpectedCount expectedCount) {
        return new OcppServerResponse(requestExpectation, expectedCount, requestExpectationManager);
    }

    /**
     * Verify all expectations.
     */
    public void verify() {
        requestExpectationManager.verify();
    }

    /**
     * Reset all expectations.
     */
    public void reset() {
        requestExpectationManager.reset();
    }

    /**
     * Use strict verification, and fail if there are any unexpected requests or responses.
     */
    public void useStrictVerification() {
        requestExpectationManager.useStrictVerification();
    }

    /**
     * Block thread until ocpp server establishes websocket connection.
     */
    public void waitUntilConnected() {
        await().untilAsserted(() -> assertThat(ocppServerClient.isConnected()).isEqualTo(true));
    }

    /**
     * Block thread until ocpp server receives http basic authorization header.
     */
    public void waitUntilAuthorized() {
        await().untilAsserted(() -> assertThat(identityManager.hasCredentials()).isEqualTo(true));
    }

    /**
     * Getter for received credentials.
     *
     * @return map of received credentials
     */
    public Map<String, String> getReceivedCredentials() {
        return identityManager.getReceivedCredentials();
    }

    /**
     * Web-socket connection attempts.
     *
     * @return connection attempts
     */
    public int connectionAttempts() {
        return connectionAttempts.get();
    }

    /**
     * Setter for password.
     *
     * @param password
     */
    public void setNewPassword(String password) {
        this.identityManager.setPassword(password);
    }

    private HttpHandler authentication(WebSocketProtocolHandshakeHandler handshakeHandler) {
        List<AuthenticationMechanism> mechanisms = Collections.singletonList(new BasicAuthenticationMechanism("OCPP Realm"));

        return new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, new AuthenticationMechanismsHandler(
                new AuthenticationCallHandler(handshakeHandler), mechanisms));
    }

    /**
     * Create a builder class in order to configure ocpp mock server.
     *
     * @return {@link OcppServerMockBuilder} instance
     */
    public static OcppServerMockBuilder builder() {
        return new OcppServerMockBuilder();
    }

    /**
     * Builder class for configuring ocpp mock server.
     */
    public static class OcppServerMockBuilder {

        private String hostname;
        private int port;
        private String path;
        private OcppServerClient ocppServerClient;
        private String username;
        private String password;

        public OcppServerMockBuilder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public OcppServerMockBuilder port(int port) {
            this.port = port;
            return this;
        }

        public OcppServerMockBuilder path(String path) {
            this.path = path;
            return this;
        }

        public OcppServerMockBuilder ocppServerClient(OcppServerClient ocppServerClient) {
            this.ocppServerClient = ocppServerClient;
            return this;
        }

        public OcppServerMockBuilder username(String username) {
            this.username = username;
            return this;
        }

        public OcppServerMockBuilder password(String password) {
            this.password = password;
            return this;
        }

        public OcppMockServer build() {
            return new OcppMockServer(this);
        }

    }
}
