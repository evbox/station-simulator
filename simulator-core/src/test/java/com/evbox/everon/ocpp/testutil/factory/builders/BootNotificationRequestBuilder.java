package com.evbox.everon.ocpp.testutil.factory.builders;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.testutil.constants.BootNotificationConstants;
import com.evbox.everon.ocpp.v20.message.station.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.ChargingStation;
import com.evbox.everon.ocpp.v20.message.station.Modem;

import static com.evbox.everon.ocpp.testutil.constants.BootNotificationConstants.*;

public class BootNotificationRequestBuilder {

    public static final BootNotificationRequest DEFAULT_INSTANCE = createBootNotificationRequest()
            .serialNumber(BootNotificationConstants.DEFAULT_SERIAL_NUMBER)
            .model(DEFAULT_MODEL)
            .iccid(DEFAULT_MODEM_ICCID)
            .imsi(DEFAULT_MODEM_IMSI)
            .vendorName(DEFAULT_VENDOR_NAME)
            .firmwareVersion(DEFAULT_FIRMWARE_VERSION)
            .reason(BootNotificationRequest.Reason.POWER_UP)
            .build();

    private String serialNumber;
    private String model;
    private String iccid;
    private String imsi;
    private String vendorName;
    private String firmwareVersion;
    private BootNotificationRequest.Reason reason;

    private BootNotificationRequestBuilder() {

    }

    public static BootNotificationRequestBuilder createBootNotificationRequest() {
        return new BootNotificationRequestBuilder();
    }

    public BootNotificationRequestBuilder serialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public BootNotificationRequestBuilder model(String model) {
        this.model = model;
        return this;
    }

    public BootNotificationRequestBuilder iccid(String iccid) {
        this.iccid = iccid;
        return this;
    }

    public BootNotificationRequestBuilder imsi(String imsi) {
        this.imsi = imsi;
        return this;
    }

    public BootNotificationRequestBuilder vendorName(String vendorName) {
        this.vendorName = vendorName;
        return this;
    }

    public BootNotificationRequestBuilder firmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
        return this;
    }

    public BootNotificationRequestBuilder reason(BootNotificationRequest.Reason reason) {
        this.reason = reason;
        return this;
    }

    public BootNotificationRequest build() {
        ChargingStation chargingStation = new ChargingStation()
                .withSerialNumber(new CiString.CiString20(serialNumber))
                .withModel(new CiString.CiString20(model))
                .withVendorName(new CiString.CiString50(vendorName))
                .withFirmwareVersion(new CiString.CiString50(firmwareVersion))
                .withModem(new Modem().withIccid(new CiString.CiString20(iccid)).withImsi(new CiString.CiString20(imsi)));

        return new BootNotificationRequest()
                .withChargingStation(chargingStation)
                .withReason(reason);
    }

}
