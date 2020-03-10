package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.CustomerDataUtils;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.station.CustomerInformationRequest;
import com.evbox.everon.ocpp.v20.message.station.CustomerInformationResponse;
import com.evbox.everon.ocpp.v20.message.station.NotifyCustomerInformationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerInformationRequestHandlerTest {

    private static final CiString.CiString512 NO_CUSTOMER_DATA_FOUND = new CiString.CiString512("No customer data found!");
    private static final CiString.CiString512 CUSTOMER_DATA_CLEARED = new CiString.CiString512("Customer data cleared!");

    @Mock
    private StationMessageSender stationMessageSender;

    @InjectMocks
    private CustomerInformationRequestHandler customerInformationRequestHandler;

    @Captor
    private ArgumentCaptor<CustomerInformationResponse> customerInformationResponseArgumentCaptor = ArgumentCaptor.forClass(CustomerInformationResponse.class);

    @Captor
    private ArgumentCaptor<NotifyCustomerInformationRequest> notifyCustomerInformationResponseArgumentCaptor = ArgumentCaptor.forClass(NotifyCustomerInformationRequest.class);

    @AfterEach
    void setUp() {
        CustomerDataUtils.clearAll();
    }

    @Test
    public void testHandleNoCustomerData() {
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withRequestId(123)
                .withClear(false)
                .withCustomerIdentifier(new CiString.CiString64("ID12345"))
                .withReport(true)
        );

        verify(stationMessageSender).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());
        verify(stationMessageSender).sendNotifyCustomerInformationRequest(notifyCustomerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();
        NotifyCustomerInformationRequest notifyCustomerInformationRequest = notifyCustomerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.REJECTED);

        assertThat(notifyCustomerInformationRequest.getData()).isEqualTo(NO_CUSTOMER_DATA_FOUND);
        assertThat(notifyCustomerInformationRequest.getAdditionalProperties()).isEmpty();
        assertThat(notifyCustomerInformationRequest.getRequestId()).isEqualTo(123);
        assertThat(notifyCustomerInformationRequest.getSeqNo()).isEqualTo(0);
        assertThat(notifyCustomerInformationRequest.getTbc()).isFalse();
    }

    @Test
    public void testHandleSingleReportNoClear() {
        CiString.CiString64 customerIdentification = new CiString.CiString64("ID123456");
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withRequestId(123)
                .withClear(false)
                .withCustomerIdentifier(customerIdentification)
                .withReport(true)
        );

        verify(stationMessageSender).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());
        verify(stationMessageSender).sendNotifyCustomerInformationRequest(notifyCustomerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();
        NotifyCustomerInformationRequest notifyCustomerInformationRequest = notifyCustomerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.ACCEPTED);

        assertThat(notifyCustomerInformationRequest.getData()).isEqualTo(CustomerDataUtils.getCustomerInformation(customerIdentification.toString(), "", "").get(0));
        assertThat(notifyCustomerInformationRequest.getAdditionalProperties()).isEmpty();
        assertThat(notifyCustomerInformationRequest.getRequestId()).isEqualTo(123);
        assertThat(notifyCustomerInformationRequest.getSeqNo()).isZero();
        assertThat(notifyCustomerInformationRequest.getTbc()).isFalse();
    }

    @Test
    public void testHandleNoReportAndNoClear() {
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withRequestId(123)
                .withClear(false)
                .withCustomerIdentifier(new CiString.CiString64("ID12345"))
                .withReport(false)
        );

        verify(stationMessageSender).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.REJECTED);
    }

    @Test
    public void testHandleMultipleReportsNoClear() {
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withIdToken(new IdToken().withIdToken(new CiString.CiString36("idToken1")))
                .withRequestId(123)
                .withClear(false)
                .withReport(true)
        );

        verify(stationMessageSender).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());
        verify(stationMessageSender, atLeastOnce()).sendNotifyCustomerInformationRequest(notifyCustomerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();
        NotifyCustomerInformationRequest notifyCustomerInformationRequest = notifyCustomerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.ACCEPTED);

        List<CiString.CiString512> data = CustomerDataUtils.getCustomerInformation("", "idToken1", "");

        CiString.CiString512 lastElement = data.get(data.size() - 1);
        data.remove(lastElement);

        assertThat(notifyCustomerInformationRequest.getData()).isEqualTo(lastElement);
        assertThat(notifyCustomerInformationRequest.getAdditionalProperties()).isEmpty();
        assertThat(notifyCustomerInformationRequest.getTbc()).isFalse();
        assertThat(notifyCustomerInformationRequest.getSeqNo()).isEqualTo(data.size());
        assertThat(notifyCustomerInformationRequest.getRequestId()).isEqualTo(123);
    }

    @Test
    public void testHandleSingleReportAndClear() {
        CiString.CiString64 customerIdentification = new CiString.CiString64("ID123456");
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withRequestId(123)
                .withClear(true)
                .withCustomerIdentifier(customerIdentification)
                .withReport(true)
        );

        verify(stationMessageSender, atLeast(2)).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());
        verify(stationMessageSender, atLeast(2)).sendNotifyCustomerInformationRequest(notifyCustomerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();
        NotifyCustomerInformationRequest notifyCustomerInformationRequest = notifyCustomerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.ACCEPTED);

        assertDataCleared(customerIdentification, notifyCustomerInformationRequest);
    }

    @Test
    public void testHandleNoReportAndClear() {
        CiString.CiString64 customerIdentification = new CiString.CiString64("ID123456");
        customerInformationRequestHandler.handle("123", new CustomerInformationRequest()
                .withRequestId(123)
                .withClear(true)
                .withCustomerIdentifier(customerIdentification)
                .withReport(false)
        );

        verify(stationMessageSender, atMostOnce()).sendCallResult(anyString(), customerInformationResponseArgumentCaptor.capture());
        verify(stationMessageSender, atMostOnce()).sendNotifyCustomerInformationRequest(notifyCustomerInformationResponseArgumentCaptor.capture());

        CustomerInformationResponse customerInformationResponse = customerInformationResponseArgumentCaptor.getValue();
        NotifyCustomerInformationRequest notifyCustomerInformationRequest = notifyCustomerInformationResponseArgumentCaptor.getValue();

        assertCustomerInformationResponse(customerInformationResponse, CustomerInformationResponse.Status.ACCEPTED);

        assertDataCleared(customerIdentification, notifyCustomerInformationRequest);
    }

    private void assertCustomerInformationResponse(CustomerInformationResponse customerInformationResponse, CustomerInformationResponse.Status status) {
        assertThat(customerInformationResponse.getStatus()).isEqualTo(status);
        assertThat(customerInformationResponse.getAdditionalProperties()).isEmpty();
    }

    private void assertDataCleared(CiString.CiString64 customerIdentification, NotifyCustomerInformationRequest notifyCustomerInformationRequest) {
        assertThat(CustomerDataUtils.getCustomerInformation(customerIdentification.toString(), "", "").size()).isZero();
        assertThat(notifyCustomerInformationRequest.getData()).isEqualTo(CUSTOMER_DATA_CLEARED);
        assertThat(notifyCustomerInformationRequest.getAdditionalProperties()).isEmpty();
        assertThat(notifyCustomerInformationRequest.getRequestId()).isEqualTo(123);
        assertThat(notifyCustomerInformationRequest.getSeqNo()).isZero();
        assertThat(notifyCustomerInformationRequest.getTbc()).isFalse();
    }

}
