package com.evbox.everon.ocpp.mock;

import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.mock.csms.OcppMockServer;
import com.evbox.everon.ocpp.mock.csms.OcppServerClient;
import com.evbox.everon.ocpp.mock.csms.exchange.BootNotification;
import com.evbox.everon.ocpp.mock.expect.ExpectedCount;
import com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration.StationConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration.MeterValuesConfiguration;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest.Reason.POWER_UP;

public class StationSimulatorSetUp  {

    private static final int ASSERT_TIMEOUT_IN_SECONDS = 10;
    protected ExpectedCount stationBootCount = atLeastOnce();

    protected OcppServerClient ocppServerClient = new OcppServerClient();

    protected OcppMockServer ocppMockServer = OcppMockServer.builder()
            .hostname(StationConstants.HOST)
            .port(StationConstants.PORT)
            .path(StationConstants.PATH)
            .ocppServerClient(ocppServerClient)
            .username(STATION_ID)
            .password(BASIC_AUTH_PASSWORD)
            .build();

    protected StationSimulatorRunner stationSimulatorRunner;

    private final List<String> txStartStopPoints = Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED.toString(), TxStartStopPointVariableValues.EV_CONNECTED.toString());

    @BeforeEach
    void setUp() {
        Awaitility.setDefaultTimeout(ASSERT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        ocppMockServer.start();

        StationConfiguration stationConfiguration = SimulatorConfigCreator.createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS, getMeterValuesConfiguration());
        stationConfiguration.getComponentsConfiguration().getTxCtrlr().setEvConnectionTimeOutSec(getEVConnectionTimeOutSec());
        stationConfiguration.getComponentsConfiguration().getTxCtrlr().setTxStartPoint(txStartStopPoints);
        stationConfiguration.getComponentsConfiguration().getTxCtrlr().setTxStopPoint(txStartStopPoints);

        SimulatorConfiguration simulatorConfiguration = SimulatorConfigCreator.createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(StationConstants.OCPP_SERVER_URL, simulatorConfiguration);

        // Station always needs to send boot notification on startup
        ocppMockServer
                .when(BootNotification.request(POWER_UP), stationBootCount)
                .thenReturn(BootNotification.response());
    }

    @AfterEach
    void tearDown() {
        stationSimulatorRunner.stop();
        ocppMockServer.stop();
        ocppMockServer.reset();
    }

    protected void triggerUserAction(String stationId, UserMessage action) {
        stationSimulatorRunner.getStation(stationId).sendMessage(new StationMessage(stationId, StationMessage.Type.USER_ACTION, action));
    }

    protected int getEVConnectionTimeOutSec() {
        return 10;
    }

    protected MeterValuesConfiguration getMeterValuesConfiguration() {
        return MeterValuesConfiguration.builder()
                                        .sendMeterValuesIntervalSec(0)
                                        .consumptionWattHour(100)
                                        .build();
    }

}
