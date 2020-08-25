package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v201.message.centralserver.Attribute;
import com.evbox.everon.ocpp.v201.message.centralserver.Component;
import com.evbox.everon.ocpp.v201.message.centralserver.Variable;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import com.evbox.everon.ocpp.v201.message.station.EVSE;
import com.evbox.everon.ocpp.v201.message.station.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import static java.util.Collections.singletonList;

public class OcppMessageFactory {

    public static GetVariablesRequestBuilder createGetVariablesRequest() {
        return new GetVariablesRequestBuilder();
    }

    public static SetVariablesRequestBuilder createSetVariablesRequest() {
        return new SetVariablesRequestBuilder();
    }

    public static ResetRequestBuilder createResetRequest() {
        return new ResetRequestBuilder();
    }
    public static RequestStartTransactionRequestBuilder createRequestStartTransactionBuilder() {
        return new RequestStartTransactionRequestBuilder();
    }

    public static GetVariablesResponseBuilder createGetVariablesResponse() {
        return new GetVariablesResponseBuilder();
    }

    public static SetVariablesResponseBuilder createSetVariablesResponse() {
        return new SetVariablesResponseBuilder();
    }

    public static TransactionEventBuilder createTransactionEventRequest() {
        return new TransactionEventBuilder();
    }

    public static AuthorizeRequestBuilder createAuthorizeRequest() {
        return new AuthorizeRequestBuilder();
    }

    public static class GetVariablesRequestBuilder extends SkeletonBuilder<GetVariablesRequestBuilder> {

        public GetVariablesRequest build() {

            GetVariableData getVariableDatum = new GetVariableData();
            getVariableDatum.setComponent(component);
            getVariableDatum.setVariable(variable);
            GetVariablesRequest getVariablesRequest = new GetVariablesRequest();
            getVariablesRequest.setGetVariableData(singletonList(getVariableDatum));

            return getVariablesRequest;
        }
    }

    public static class SetVariablesRequestBuilder extends SkeletonBuilder<SetVariablesRequestBuilder> {

        private CiString.CiString1000 attributeValue;
        private Attribute attributeType = Attribute.ACTUAL;

        public SetVariablesRequestBuilder withAttributeValue(String attributeValue) {
            this.attributeValue = new CiString.CiString1000(attributeValue);
            return this;
        }

        public SetVariablesRequestBuilder withAttributeType(Attribute attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public SetVariablesRequest build() {
            SetVariableData setVariableDatum = new SetVariableData();
            setVariableDatum.setComponent(component);
            setVariableDatum.setVariable(variable);
            setVariableDatum.setAttributeValue(attributeValue);
            setVariableDatum.setAttributeType(attributeType);
            SetVariablesRequest setVariablesRequest = new SetVariablesRequest();
            setVariablesRequest.setSetVariableData(singletonList(setVariableDatum));

            return setVariablesRequest;
        }
    }

    public static class ResetRequestBuilder {

        private Reset type;

        public ResetRequestBuilder withType(Reset type) {
            this.type = type;
            return this;
        }

        public ResetRequest build() {
            ResetRequest resetRequest = new ResetRequest();
            resetRequest.setType(type);
            return resetRequest;
        }
    }

    public static class RequestStartTransactionRequestBuilder {

        private Integer evseId;
        private Integer remoteStartId;
        private IdToken idToken;

        public RequestStartTransactionRequestBuilder withEvseId(Integer evseId) {
            this.evseId = evseId;
            return this;
        }

        public RequestStartTransactionRequestBuilder withRemoteStartId(Integer remoteStartId) {
            this.remoteStartId = remoteStartId;
            return this;
        }

        public RequestStartTransactionRequestBuilder withIdToken(IdToken idToken) {
            this.idToken = idToken;
            return this;
        }

        public RequestStartTransactionRequest build() {
            RequestStartTransactionRequest request = new RequestStartTransactionRequest();
            request.setEvseId(evseId);
            request.setIdToken(idToken);
            request.setRemoteStartId(remoteStartId);
            return request;
        }
    }

    public static class GetVariablesResponseBuilder extends SkeletonBuilder<GetVariablesResponseBuilder> {

        private GetVariableStatus attributeStatus;
        private CiString.CiString2500 attributeValue;

        public GetVariablesResponseBuilder withAttributeStatus(GetVariableStatus attributeStatus) {
            this.attributeStatus = attributeStatus;
            return this;
        }

        public GetVariablesResponseBuilder withAttributeValue(String attributeValue) {
            this.attributeValue = new CiString.CiString2500(attributeValue);
            return this;
        }

        public GetVariablesResponse build() {

            return new GetVariablesResponse().withGetVariableResult(
                    singletonList(new GetVariableResult()
                            .withComponent(component)
                            .withVariable(variable)
                            .withAttributeStatus(attributeStatus)
                            .withAttributeValue(attributeValue)
                    )
            );

        }

    }

    public static class SetVariablesResponseBuilder extends SkeletonBuilder<SetVariablesResponseBuilder> {

        private SetVariableStatus attributeStatus;

        public SetVariablesResponseBuilder withAttributeStatus(SetVariableStatus attributeStatus) {
            this.attributeStatus = attributeStatus;
            return this;
        }

        public SetVariablesResponse build() {

            return new SetVariablesResponse().withSetVariableResult(
                    singletonList(new SetVariableResult()
                            .withComponent(component)
                            .withVariable(variable)
                            .withAttributeStatus(attributeStatus))
            );

        }

    }

    public static class TransactionEventBuilder {

        private TransactionEvent eventType;
        private int sampledValue;
        private Instant meterValueTimestamp;
        private Instant timestamp;
        private TriggerReason triggerReason;
        private Integer seqNo;
        private String transactionId;
        private int evseId;
        private String tokenId;
        private IdTokenType tokenType;

        public TransactionEventBuilder withEventType(TransactionEvent eventType) {
            this.eventType = eventType;
            return this;
        }

        public TransactionEventBuilder withSampledValue(int sampledValue) {
            this.sampledValue = sampledValue;
            return this;
        }

        public TransactionEventBuilder withMeterValueTimestamp(Instant meterValueTimestamp) {
            this.meterValueTimestamp = meterValueTimestamp;
            return this;
        }

        public TransactionEventBuilder withTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransactionEventBuilder withTriggerReason(TriggerReason triggerReason) {
            this.triggerReason = triggerReason;
            return this;
        }

        public TransactionEventBuilder withSeqNo(int seqNo) {
            this.seqNo = seqNo;
            return this;
        }

        public TransactionEventBuilder withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionEventBuilder withEvseId(int evseId) {
            this.evseId = evseId;
            return this;
        }

        public TransactionEventBuilder withTokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public TransactionEventBuilder withTokenType(IdTokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TransactionEventRequest build() {

            MeterValue meterValue = new MeterValue()
                    .withSampledValue(singletonList(new SampledValue().withValue(BigDecimal.valueOf(sampledValue))))
                    .withTimestamp(meterValueTimestamp.atZone(ZoneOffset.UTC));

            IdToken idToken = new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(tokenType);

            return new TransactionEventRequest()
                    .withEventType(eventType)
                    .withMeterValue(Collections.singletonList(meterValue))
                    .withTimestamp(timestamp.atZone(ZoneOffset.UTC))
                    .withTriggerReason(triggerReason)
                    .withSeqNo(seqNo)
                    .withTransactionInfo(new Transaction().withTransactionId(new CiString.CiString36(transactionId)))
                    .withEvse(new EVSE().withId(evseId))
                    .withIdToken(idToken);

        }

    }

    public static class AuthorizeRequestBuilder {

        private String tokenId;
        private int evseId;

        public AuthorizeRequestBuilder withTokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public AuthorizeRequestBuilder withEvseId(int evseId) {
            this.evseId = evseId;
            return this;
        }

        public AuthorizeRequest build() {
            return new AuthorizeRequest()
                    .withIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdTokenType.ISO_14443));
            //TODO: check removed evse id here. Present in OCPP 2.0 but not in OCPP 2.0.1
        }
    }

    private static class SkeletonBuilder<T> {

        Component component;
        Variable variable;

        public T withComponent(String name) {
            this.component = new Component().withName(new CiString.CiString50(name));
            return (T) this;
        }

        public T withVariable(String name) {
            this.variable = new Variable().withName(new CiString.CiString50(name));
            return (T) this;
        }
    }
}
