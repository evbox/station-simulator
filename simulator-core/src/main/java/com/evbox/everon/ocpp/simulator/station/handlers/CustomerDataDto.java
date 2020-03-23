package com.evbox.everon.ocpp.simulator.station.handlers;

import com.evbox.everon.ocpp.common.CiString;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerDataDto {
    private final String customerIdentifier;
    private final String idToken;
    private final String customerCertificateSerialNumber;
    private final CiString.CiString512 customerInformation;
}
