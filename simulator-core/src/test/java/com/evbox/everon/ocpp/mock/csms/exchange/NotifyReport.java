package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.station.NotifyReportRequest;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.emptyResponse;
import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.NOTIFY_REPORT;

public class NotifyReport {

    /**
     * NotifyReportRequest with any configuration.
     *
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> request() {
        return request -> equalsType(request, NOTIFY_REPORT);
    }

    /**
     * NotifyReportRequest with given configuration.
     *
     * @param seqNo notify report page number
     * @param tbc   to be continued
     * @return checks whether an incoming request is NotifyReportRequest or not.
     */
    public static Predicate<Call> request(int seqNo, boolean tbc) {
        return request -> equalsType(request, NOTIFY_REPORT) && equalsSeqNo(request, seqNo) && equalsTbc(request, tbc);
    }

    /**
     * Create a response for NotifyReport.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return emptyResponse();
    }

    private static boolean equalsSeqNo(Call request, int seqNo) {
        return ((NotifyReportRequest) request.getPayload()).getSeqNo() == seqNo;
    }

    private static boolean equalsTbc(Call request, boolean tbc) {
        return ((NotifyReportRequest) request.getPayload()).getTbc() == tbc;
    }
}
