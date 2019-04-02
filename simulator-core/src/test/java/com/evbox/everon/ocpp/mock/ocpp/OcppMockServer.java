package com.evbox.everon.ocpp.mock.ocpp;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.message.CallResult;
import com.evbox.everon.ocpp.mock.expect.ExpectedCount;
import com.evbox.everon.ocpp.mock.expect.RequestExpectationManager;
import com.evbox.everon.ocpp.mock.expect.ResponseExpectationManager;
import com.evbox.everon.ocpp.mock.match.ResponseMatcher;
import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
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
    private final ResponseExpectationManager responseExpectationManager = new ResponseExpectationManager();
    private final RequestResponseSynchronizer requestResponseSynchronizer = new RequestResponseSynchronizer();

    private final OcppServerClient ocppServerClient;
    private final String hostname;
    private final int port;
    private final String path;

    private OcppMockServer(OcppServerMockBuilder builder) {
        Objects.requireNonNull(builder.hostname);
        Objects.requireNonNull(builder.path);
        Objects.requireNonNull(builder.ocppServerClient);

        this.ocppServerClient = builder.ocppServerClient;
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.path = builder.path;

    }

    /**
     * Start mock server.
     */
    public void start() {
        String targetUrl = "ws://" + hostname + ":" + port + path + "/";

        server = Undertow.builder()
                .addHttpListener(port, hostname)
                .setHandler(
                        path().addPrefixPath(path, websocket((exchange, channel) -> {
                            String stationId = channel.getUrl().replace(targetUrl, "");
                            channel.getReceiveSetter().set(new OcppReceiveListener(requestExpectationManager, responseExpectationManager, ocppServerClient, requestResponseSynchronizer));
                            channel.resumeReceives();

                            ocppServerClient.putIfAbsent(stationId, new WebSocketSender(channel, requestResponseSynchronizer));

                        })))
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
     * Accepts a predicate that is responsible for response expectation.
     * By default: expected count is 1
     *
     * @param responseExpectation a response expectation predicate
     * @return {@link OcppServerResponse} instance
     */
    public OcppMockServer expectResponseFromStation(Predicate<CallResult> responseExpectation) {
        return expectResponseFromStation(responseExpectation, once());
    }

    /**
     * Accepts a predicate that is responsible for response expectation.
     *
     * @param responseExpectation a response expectation predicate
     * @param expectedCount      expected count
     * @return {@link OcppServerResponse} instance
     */
    public OcppMockServer expectResponseFromStation(Predicate<CallResult> responseExpectation, ExpectedCount expectedCount) {
        responseExpectationManager.add(new ResponseMatcher(responseExpectation, expectedCount));
        return this;
    }

    /**
     * Verify all expectations.
     */
    public void verify() {
        requestExpectationManager.verify();
        responseExpectationManager.verify();
    }

    /**
     * Reset all expectations.
     */
    public void reset() {
        requestExpectationManager.reset();
        responseExpectationManager.reset();
    }

    /**
     * Use strict verification, and fail if there are any unexpected requests or responses.
     */
    public void useStrictVerification() {
        requestExpectationManager.useStrictVerification();
        responseExpectationManager.useStrictVerification();
    }

    /**
     * Block thread until ocpp server establishes websocket connection.
     */
    public void waitUntilConnected() {
        await().untilAsserted(() -> assertThat(ocppServerClient.isConnected()).isEqualTo(true));
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

        public OcppMockServer build() {
            return new OcppMockServer(this);
        }

    }
}
