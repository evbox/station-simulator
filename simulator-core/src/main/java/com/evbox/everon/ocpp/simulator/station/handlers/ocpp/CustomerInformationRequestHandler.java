package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.CustomerDataUtils;
import com.evbox.everon.ocpp.v20.message.common.IdToken;
import com.evbox.everon.ocpp.v20.message.station.CustomerCertificate;
import com.evbox.everon.ocpp.v20.message.station.CustomerInformationRequest;
import com.evbox.everon.ocpp.v20.message.station.CustomerInformationResponse;
import com.evbox.everon.ocpp.v20.message.station.NotifyCustomerInformationRequest;
import lombok.RequiredArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.evbox.everon.ocpp.common.CiString.CiString512;
import static com.evbox.everon.ocpp.common.CiString.CiString64;
import static com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.CustomerDataUtils.*;

@RequiredArgsConstructor
public class CustomerInformationRequestHandler implements OcppRequestHandler<CustomerInformationRequest> {

    private static final CiString512 NO_CUSTOMER_DATA_FOUND = new CiString512("No customer data found!");
    private static final CiString512 CUSTOMER_DATA_CLEARED = new CiString512("Customer data cleared!");

    private StationMessageSender stationMessageSender;

    public CustomerInformationRequestHandler(final StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        initializeCustomerData();
    }

    @Override
    public void handle(String callId, CustomerInformationRequest request) {
        final var customerIdentifier = Optional.ofNullable(request.getCustomerIdentifier()).map(CiString64::toString).orElse("");
        final var idToken = Optional.ofNullable(request.getIdToken()).map(IdToken::getIdToken).map(CiString::toString).orElse("");
        final var certificateSerialNumber = Optional.ofNullable(request.getCustomerCertificate()).map(CustomerCertificate::getSerialNumber).map(CiString::toString).orElse("");

        verifyAndSendCustomerDataReport(callId, customerIdentifier, idToken, certificateSerialNumber, request.getRequestId(), request.getClear(), request.getReport());
    }

    private void verifyAndSendCustomerDataReport(final String callId, final String customerIdentifier, final String idToken, final String certificateSerialNumber, final Integer requestId, final Boolean clear, final Boolean report) {
        if (!report && !clear) {
            sendCustomerInformationResponse(callId, CustomerInformationResponse.Status.REJECTED);
        } else if (hasCustomerData(customerIdentifier, idToken, certificateSerialNumber)) {
            if (report) {
                sendCustomerInformationResponse(callId, CustomerInformationResponse.Status.ACCEPTED);
                sendCustomerDataReport(customerIdentifier, idToken, certificateSerialNumber, requestId);
            }
            if (clear) {
                clearCustomerData(customerIdentifier, idToken, certificateSerialNumber, callId, requestId);
            }
        } else {
            sendCustomerInfoNotFoundRequest(callId, requestId);
        }
    }

    private void sendCustomerInfoNotFoundRequest(final String callId, final Integer requestId) {
        sendCustomerInformationResponse(callId, CustomerInformationResponse.Status.REJECTED);
        var baseRequest = createNotifyCustomerInformationBaseRequest(requestId);
        stationMessageSender.sendNotifyCustomerInformationRequest(baseRequest.withData(NO_CUSTOMER_DATA_FOUND));
    }

    private void clearCustomerData(final String customerIdentifier, final String idToken, final String certificateSerialNumber, final String callId, final Integer requestId) {
        CustomerDataUtils.clearCustomerData(customerIdentifier, idToken, certificateSerialNumber);
        sendCustomerInformationResponse(callId, CustomerInformationResponse.Status.ACCEPTED);
        stationMessageSender.sendNotifyCustomerInformationRequest(createNotifyCustomerInformationBaseRequest(requestId).withData(CUSTOMER_DATA_CLEARED));
    }

    private void sendCustomerInformationResponse(final String callId, final CustomerInformationResponse.Status status) {
        stationMessageSender.sendCallResult(callId, new CustomerInformationResponse().withStatus(status));
    }

    private void sendCustomerDataReport(final String customerIdentifier, String idToken, String certificateSerialNumber, Integer requestId) {
        var customerInformation = getCustomerInformation(customerIdentifier, idToken, certificateSerialNumber);
        sendNotifyCustomerInformationReport(customerInformation, createNotifyCustomerInformationBaseRequest(requestId));
    }

    private void sendNotifyCustomerInformationReport(final List<CiString512> customerInformation, NotifyCustomerInformationRequest baseRequest) {
        if (customerInformation.size() > 1) {
            sendMultiplePageReport(customerInformation, baseRequest);
        } else {
            customerInformation.forEach(data -> stationMessageSender.sendNotifyCustomerInformationRequest(baseRequest.withData(data)));
        }
    }

    private void sendMultiplePageReport(List<CiString512> customerInformation, NotifyCustomerInformationRequest baseRequest) {
        var lastElement = customerInformation.get(customerInformation.size() - 1);
        customerInformation.remove(lastElement);
        customerInformation.forEach(data -> stationMessageSender.sendNotifyCustomerInformationRequest(baseRequest.withData(data).withTbc(true).withSeqNo(customerInformation.indexOf(data))));
        stationMessageSender.sendNotifyCustomerInformationRequest(baseRequest.withTbc(false).withData(lastElement).withSeqNo(customerInformation.size()));
    }

    private NotifyCustomerInformationRequest createNotifyCustomerInformationBaseRequest(final Integer requestId) {
        return new NotifyCustomerInformationRequest()
                .withGeneratedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .withRequestId(requestId)
                .withSeqNo(0)
                .withTbc(false);
    }

}