package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.station.ReportDatum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportRequest.ReportBase.*;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.ACCEPTED;
import static com.evbox.everon.ocpp.v20.message.station.GetBaseReportResponse.Status.NOT_SUPPORTED;
import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZonedDateTime.ofInstant;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Remove once SummaryInventory is implemented
public class GetBaseReportRequestHandlerTest {

    private static final int REQUEST_ID = 300;
    private static final ReportDatum REPORT_DATUM = new ReportDatum();

    @Mock
    StationMessageSender stationMessageSenderMock;
    @Mock
    StationComponentsHolder componentsHolderMock;
    @Mock
    Clock clock;
    @Captor
    ArgumentCaptor<GetBaseReportResponse> messageCaptor;
    @InjectMocks
    GetBaseReportRequestHandler requestHandler;

    @BeforeEach
    void initClockMocks() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Get base report should return not supported")
    void verifyCallResultSummaryInventory() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new GetBaseReportRequest().withReportBase(SUMMARY_INVENTORY));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    @DisplayName("Get base report should return all variables")
    void verifyCallResultFullInventory() {
        when(componentsHolderMock.generateReportData(false)).thenReturn(newArrayList(REPORT_DATUM, REPORT_DATUM));

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(FULL_INVENTORY).withRequestId(REQUEST_ID);
        requestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        verify(stationMessageSenderMock).sendNotifyReport(REQUEST_ID, true, 0, now(), singletonList(REPORT_DATUM));
        verify(stationMessageSenderMock).sendNotifyReport(REQUEST_ID, false, 1, now(), singletonList(REPORT_DATUM));
        assertThat(messageCaptor.getValue().getStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    @DisplayName("Get base report should return only configurable variables")
    void verifyCallResultConfigurationInventory() {
        when(componentsHolderMock.generateReportData(true)).thenReturn(singletonList(REPORT_DATUM));

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(CONFIGURATION_INVENTORY).withRequestId(REQUEST_ID);
        requestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        verify(stationMessageSenderMock).sendNotifyReport(REQUEST_ID, false, 0, now(), singletonList(REPORT_DATUM));
        assertThat(messageCaptor.getValue().getStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    @DisplayName("Get base report should return successfully when request id is not specified")
    void verifyCallResultNoRequestId() {
        when(componentsHolderMock.generateReportData(true)).thenReturn(singletonList(REPORT_DATUM));

        GetBaseReportRequest request = new GetBaseReportRequest().withReportBase(CONFIGURATION_INVENTORY);
        requestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationMessageSenderMock).sendNotifyReport(null, false, 0, now(),singletonList(REPORT_DATUM));
    }

    private ZonedDateTime now() {
        return ofInstant(clock.instant(), clock.getZone());
    }
}
