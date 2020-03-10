package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.handlers.CustomerDataDto;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class CustomerDataUtils {

    private static final List<CustomerDataDto> CUSTOMER_DATA_DTO_LIST = new ArrayList<>();
    private static final String ID_TOKEN = "idToken1";
    private static final String CERTIFICATE_SERIAL_NUMBER = "SN123456";
    private static final String CUSTOMER_IDENTIFIER = "ID123456";

    public static void initializeCustomerData() {
        CUSTOMER_DATA_DTO_LIST.addAll(Arrays.asList(
                CustomerDataDto.builder()
                        .idToken(ID_TOKEN)
                        .customerInformation(new CiString.CiString512("Dummy customer information related to idToken1"))
                        .build(),
                CustomerDataDto.builder()
                        .idToken(ID_TOKEN)
                        .customerInformation(new CiString.CiString512("Additional dummy customer information related to idToken1"))
                        .build(),
                CustomerDataDto.builder()
                        .customerCertificateSerialNumber(CERTIFICATE_SERIAL_NUMBER)
                        .customerInformation(new CiString.CiString512("Dummy customer information related to customer certificate serial number SN123456"))
                        .build(),
                CustomerDataDto.builder()
                        .customerIdentifier(CUSTOMER_IDENTIFIER)
                        .customerInformation(new CiString.CiString512("Dummy customer information related to customer with customerIdentifier ID123456"))
                        .build()
        ));
    }

    public static boolean hasCustomerData(final String customerIdentifier, final String idToken, final String certificateSerialNumber) {
        return CUSTOMER_DATA_DTO_LIST.stream()
                .anyMatch(cd -> compareData(customerIdentifier, idToken, certificateSerialNumber, cd));
    }

    public static void clearCustomerData(final String customerIdentifier, final String idToken, final String certificateSerialNumber) {
        CUSTOMER_DATA_DTO_LIST.removeIf(cd -> compareData(customerIdentifier, idToken, certificateSerialNumber, cd));
    }

    public static List<CiString.CiString512> getCustomerInformation(final String customerIdentifier, final String idToken, final String certificateSerialNumber) {
        return CUSTOMER_DATA_DTO_LIST.stream()
                .filter(cd -> compareData(customerIdentifier, idToken, certificateSerialNumber, cd))
                .map(CustomerDataDto::getCustomerInformation).collect(Collectors.toList());
    }

    public static void clearAll() {
        CUSTOMER_DATA_DTO_LIST.clear();
    }

    private static boolean compareData(String customerIdentifier, String idToken, String certificateSerialNumber, CustomerDataDto customerData) {
        return customerIdentifier.equals(customerData.getCustomerIdentifier()) || idToken.equals(customerData.getIdToken()) || certificateSerialNumber.equals(customerData.getCustomerCertificateSerialNumber());
    }

}
