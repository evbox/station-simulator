package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.SignCertificateResponse;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.SIGN_CERTIFICATE;
import static com.evbox.everon.ocpp.v20.message.station.SignCertificateResponse.Status.ACCEPTED;

public class SignCertificate {

    /**
     * SignCertificate request with any csr.
     *
     * @return checks whether an incoming request is a SignCertificateRequest or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, SIGN_CERTIFICATE);
    }

    /**
     * Create SignCertificateRequest with accepted status.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return request -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(request.getMessageId())
                .withPayload(new SignCertificateResponse().withStatus(ACCEPTED))
                .toJson();
    }
}
