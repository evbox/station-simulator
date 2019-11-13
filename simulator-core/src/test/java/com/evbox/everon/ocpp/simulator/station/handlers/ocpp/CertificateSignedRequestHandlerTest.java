package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v20.message.station.CertificateSignedRequest;
import com.evbox.everon.ocpp.v20.message.station.CertificateSignedResponse;
import com.google.common.base.Splitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CertificateSignedRequestHandlerTest {

    @Mock
    StationStore stationStoreMock;
    @Mock
    StationMessageSender stationMessageSenderMock;
    @Captor
    ArgumentCaptor<CertificateSignedResponse> messageCaptor;

    @InjectMocks
    CertificateSignedRequestHandler requestHandler;

    private static final String expiredCertificate = "3082035830820240a003020102020500f8b10766300d06092a864886f70d01010b05003075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e6361301e170d3138313130383039323832345a170d3139313130373039323832345a30173115301306035504030c0c30303030303030303030304630820122300d06092a864886f70d01010105000382010f003082010a0282010100dbd865c2c144dbceab605ec2dcec80d8ac5b6e9a3ed98895c569dfffc71b7ab4e074628cf4edfc071ae1bc75f3ce88578719dbd05d8ff27a79883488325d0b7131497b518adf205cd3f16e4698fb116b613f088621eba6c7fd65b9960e882961340bf3882f37f4168a726f1f367821f7856d59658a5508769d8c39401ee349e77edf5a03a1f9b817adc17b7eaa75d560fb3cc0b5e6c3478486e2ae76db3f796be234d6f080937537a61c6a9b85c2ed520028a6c13909f386f3adc726f92fb4c92c8babf3cbc7d7e4c3b1f3b5d54f376726947761ad7aea0d33e6c4537427cf6da12f011b928b119083b6af3b797991ba519a976d22d473d65d84c09a74a722db0203010001a34d304b30090603551d1304023000301d0603551d0e04160414c078da2313232fa6293b4d31253d103f0b856972301f0603551d23041830168014fce4a2ae5d72e6af9f8ada3af4bc15b7668cdeb5300d06092a864886f70d01010b050003820101009522c037a121b020a2f577cd79ce1b77f886c24c9d952895d34c4455d1f6d396b7fc08b9a454eb049fb4e5fab0d1cd45b154ea5390110f4f28a6c6577185bb3b6c651232dc8c942466fce42b7468c2c269b6fc3709e2b7aa4733883e58310ae260112ffd5f69be04cf9fbcfc18daf0b956766486f66f2eabb5d416e7a5ce69d670b2fb2f7fbd456be27fa0bee1def27e7ff4f3c5701cfe8a47fcb48f798f6989529cbf42d09634c5666811ecb4b525d78e40678fe16ac69a478fa5d1a819586c1e7b8b3d1f3802e4857ab419f2310635ebd04ba89317f387b9f02be7fef5bf4c5498132f530fbcf96c8e48a75088dce1122e8f116fa1cc82ed131286867eb8e4\n" +
            "308203643082024c02021235300d06092a864886f70d0101050500307a310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310f300d060355040a0c06526f6f744341310f300d060355040b0c06526f6f744341310f300d06035504030c06526f6f744341311d301b06092a864886f70d010901160e726f6f74636140726f6f742e6361301e170d3139313032313133303633365a170d3230313032303133303633365a3075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e636130820122300d06092a864886f70d01010105000382010f003082010a0282010100b8025e34f372a4957af6b12ebc07475770c8dd1a9b657eeb5e9f1790b8217c3652feef703c4407895d740658a68ce26d890266d27d8115d587f7ee011aa14bd56494cadf81fecf20ba67ace4e96154b0b61a0860a0818a1d65631533e61f07d5857a6d33640e87273d5ee4b5446b55e15fb2dc1f4416ae13a07f64560ba6d51a6dfa52b7dd5f24039a7b8094c84ddb36ae7c037dc306df5b6a914a0409102611ecf0a3a5e390620fb9d6ec0ad48a1ccaf61c0257bebe6186edabeaba9bd3966f69f20a49190cf6dda28d1367fe66882aafed27fdf01bcc8adc8b629048148b84fb76cdbfc68ba5f9736090779160725be68db41822c480a7f3f0c7ea3acc27c70203010001300d06092a864886f70d010105050003820101007dacf7835ca9865f0f146acba45c97ce71386c7afc769f8f05fff59c11e813d7ca63904b1bafba386791f913b6b1be17bdadf2c23438aeb6c1a04bba9a69e0c7c7dd14db0d6c21e395e0aedd310fba5bb3746779cb0a6d4ad64979023fccf170649ac35b2ade0d199f75e6d2e2c1b43c49976298b2d79f5e1004e2cb40274110bde4ebe44f03edd5b6f8707f99ba17038f7971af8d1da21238b014eeb819580cfc33bfc4328050c62589023c79631f82925802a080ab4338fcc199283c924db682806c8cb2d5cd2834bb10a50633d6a0eae0b05ad74c7b2e51b50b06df7955528411f28b1c1f82c469b2b8e230ca63875091d8b64e387132267f66dd18bee54f";

    // Following certificates valid until 5 November 2029
    private static final String wrongSerialCertificate = "308203573082023fa003020102020465622c65300d06092a864886f70d01010b05003075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e6361301e170d3139313130383039333432355a170d3239313130353039333432355a30173115301306035504030c0c31323334353637383930304630820122300d06092a864886f70d01010105000382010f003082010a0282010100b6f5a34731f26e60d9b2fd41d802fca5cff2cf00dd00d5822f9b8e998c6cfc50f609b0d19d8156c1b069ef9917e71277415118e3b0ea8ffe9f1892591405b31cf02a6ad5193f048fe23820c6887cd556501a26a18089e77a9c54270654ac88feec850dd55b35f2a6ae2c76fe78bb41fb99eafaa135070ee61a241979e215f8103492d594fe2449b20cf5ae1a796d07be9e9d9e779aa0c1fc4585adcb3d41dda75b801123d80dc1261454535a7e9c3e8b8780a7b3d6103142cc23b600e6038887c4b9014f66b41d8e91eac2d5f662b15c2db019ad3b3ab95b821cf138ec4ef73755164cef02017ee22dac104199473c9bd64e857cf4249e70dca107d6b5bff7f90203010001a34d304b30090603551d1304023000301d0603551d0e04160414a2ac636ae8cb9b0a02110273c02c72a78af1df78301f0603551d23041830168014fce4a2ae5d72e6af9f8ada3af4bc15b7668cdeb5300d06092a864886f70d01010b0500038201010023d3b2614bdcbe0425605661f48aa76724595b17feb20d42d8e12a257cea9972c4335087abb1091244718d7cb7159cca59e1894d574c9488ab5859d61dad45c308d0fb75a6044110eebab0b0a176ea1e5d3876bb5ccf9452a7062c134228ee31ac22b5510daafded1edb7deeeeeb437048372f6e5c638bdf77ffeb75c1263d1037996264fd883512d3fe42a11bc7f151a0aaee0ea4fd4d6b13a8bf4942369e29d8905003572088445f220e070db54fc51c91e79e8468ff3438b5ae25d0ec560d22d2612bc958ddc20b61649625dcdc102debb0b9ccd66e044dd2480c5046d0489c3e164294222a628acc42b18a48456a13e68f2ba7c45ff0af6e6bd287ae5f0f\n" +
            "308203643082024c02021235300d06092a864886f70d0101050500307a310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310f300d060355040a0c06526f6f744341310f300d060355040b0c06526f6f744341310f300d06035504030c06526f6f744341311d301b06092a864886f70d010901160e726f6f74636140726f6f742e6361301e170d3139313032313133303633365a170d3230313032303133303633365a3075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e636130820122300d06092a864886f70d01010105000382010f003082010a0282010100b8025e34f372a4957af6b12ebc07475770c8dd1a9b657eeb5e9f1790b8217c3652feef703c4407895d740658a68ce26d890266d27d8115d587f7ee011aa14bd56494cadf81fecf20ba67ace4e96154b0b61a0860a0818a1d65631533e61f07d5857a6d33640e87273d5ee4b5446b55e15fb2dc1f4416ae13a07f64560ba6d51a6dfa52b7dd5f24039a7b8094c84ddb36ae7c037dc306df5b6a914a0409102611ecf0a3a5e390620fb9d6ec0ad48a1ccaf61c0257bebe6186edabeaba9bd3966f69f20a49190cf6dda28d1367fe66882aafed27fdf01bcc8adc8b629048148b84fb76cdbfc68ba5f9736090779160725be68db41822c480a7f3f0c7ea3acc27c70203010001300d06092a864886f70d010105050003820101007dacf7835ca9865f0f146acba45c97ce71386c7afc769f8f05fff59c11e813d7ca63904b1bafba386791f913b6b1be17bdadf2c23438aeb6c1a04bba9a69e0c7c7dd14db0d6c21e395e0aedd310fba5bb3746779cb0a6d4ad64979023fccf170649ac35b2ade0d199f75e6d2e2c1b43c49976298b2d79f5e1004e2cb40274110bde4ebe44f03edd5b6f8707f99ba17038f7971af8d1da21238b014eeb819580cfc33bfc4328050c62589023c79631f82925802a080ab4338fcc199283c924db682806c8cb2d5cd2834bb10a50633d6a0eae0b05ad74c7b2e51b50b06df7955528411f28b1c1f82c469b2b8e230ca63875091d8b64e387132267f66dd18bee54f";
    private static final String validCertificate = "3082035830820240a0030201020205009252d85e300d06092a864886f70d01010b05003075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e6361301e170d3139313130383039333630395a170d3239313130353039333630395a30173115301306035504030c0c30303030303030303030304630820122300d06092a864886f70d01010105000382010f003082010a0282010100dbd865c2c144dbceab605ec2dcec80d8ac5b6e9a3ed98895c569dfffc71b7ab4e074628cf4edfc071ae1bc75f3ce88578719dbd05d8ff27a79883488325d0b7131497b518adf205cd3f16e4698fb116b613f088621eba6c7fd65b9960e882961340bf3882f37f4168a726f1f367821f7856d59658a5508769d8c39401ee349e77edf5a03a1f9b817adc17b7eaa75d560fb3cc0b5e6c3478486e2ae76db3f796be234d6f080937537a61c6a9b85c2ed520028a6c13909f386f3adc726f92fb4c92c8babf3cbc7d7e4c3b1f3b5d54f376726947761ad7aea0d33e6c4537427cf6da12f011b928b119083b6af3b797991ba519a976d22d473d65d84c09a74a722db0203010001a34d304b30090603551d1304023000301d0603551d0e04160414c078da2313232fa6293b4d31253d103f0b856972301f0603551d23041830168014fce4a2ae5d72e6af9f8ada3af4bc15b7668cdeb5300d06092a864886f70d01010b05000382010100a188ec7c5ad444753780b9d59d80c875f032ee49fba2ca9b5cf22a376b5d5cb1e4214b7e311fbeb205ee5794c1638d2edc1df16e5c3327c9bea4d6386e9a8c051cc7df52a42fdaf99e852b84a638e50a5be2a0ecd52a8588067c430f5b367637cf2e8c1693ab4dccc28e9f94006a90911162599eb4ac9cafcdd844190b3253e4216a205f89ffa631f82299efd26bf2800caecbcd8d079661b29cfd56cfa65235fd7557746c0101ab737f549615cde8ffd052ef8f19ff8104dcdf718426cee7cdf4716528780c1600a5251b9cdf4fd7770d4542d0807984df1d12e38b9c8f047a9cef17ec9b0b35784f12afaf62f5b61f0aea191129339d2512dccf2713950010\n" +
            "308203643082024c02021235300d06092a864886f70d0101050500307a310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310f300d060355040a0c06526f6f744341310f300d060355040b0c06526f6f744341310f300d06035504030c06526f6f744341311d301b06092a864886f70d010901160e726f6f74636140726f6f742e6361301e170d3139313032313133303633365a170d3230313032303133303633365a3075310b3009060355040613024e4c310b300906035504080c024e4c310c300a06035504070c03414d53310e300c060355040a0c055375624341310e300c060355040b0c055375624341310e300c06035504030c055375624341311b301906092a864886f70d010901160c7375626361407375622e636130820122300d06092a864886f70d01010105000382010f003082010a0282010100b8025e34f372a4957af6b12ebc07475770c8dd1a9b657eeb5e9f1790b8217c3652feef703c4407895d740658a68ce26d890266d27d8115d587f7ee011aa14bd56494cadf81fecf20ba67ace4e96154b0b61a0860a0818a1d65631533e61f07d5857a6d33640e87273d5ee4b5446b55e15fb2dc1f4416ae13a07f64560ba6d51a6dfa52b7dd5f24039a7b8094c84ddb36ae7c037dc306df5b6a914a0409102611ecf0a3a5e390620fb9d6ec0ad48a1ccaf61c0257bebe6186edabeaba9bd3966f69f20a49190cf6dda28d1367fe66882aafed27fdf01bcc8adc8b629048148b84fb76cdbfc68ba5f9736090779160725be68db41822c480a7f3f0c7ea3acc27c70203010001300d06092a864886f70d010105050003820101007dacf7835ca9865f0f146acba45c97ce71386c7afc769f8f05fff59c11e813d7ca63904b1bafba386791f913b6b1be17bdadf2c23438aeb6c1a04bba9a69e0c7c7dd14db0d6c21e395e0aedd310fba5bb3746779cb0a6d4ad64979023fccf170649ac35b2ade0d199f75e6d2e2c1b43c49976298b2d79f5e1004e2cb40274110bde4ebe44f03edd5b6f8707f99ba17038f7971af8d1da21238b014eeb819580cfc33bfc4328050c62589023c79631f82925802a080ab4338fcc199283c924db682806c8cb2d5cd2834bb10a50633d6a0eae0b05ad74c7b2e51b50b06df7955528411f28b1c1f82c469b2b8e230ca63875091d8b64e387132267f66dd18bee54f";
    @Test
    @DisplayName("Expired certificate should be rejected")
    void verifyExpiredCertificateIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCert(stringToCiStringsList(expiredCertificate)));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedResponse.Status.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Empty certificate should be rejected")
    void verifyEmptyCertificateIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCert(stringToCiStringsList("NotACert")));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedResponse.Status.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Certificate with wrong serial number should be rejected")
    void verifyCertificateWithInvalidSerialIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCert(stringToCiStringsList(wrongSerialCertificate)));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedResponse.Status.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("V2G certificate type should be rejected")
    void verifyV2GCertificateTypeIsRejected() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withTypeOfCertificate(CertificateSignedRequest.TypeOfCertificate.V_2_G_CERTIFICATE).withCert(stringToCiStringsList(wrongSerialCertificate)));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedResponse.Status.REJECTED.value());
        assertThat(Optional.ofNullable(requestHandler.getScheduledFuture())).isEmpty();
    }

    @Test
    @DisplayName("Correct certificate type should be accepted")
    void verifyValidCertificateTypeIsCorrectlySet() {
        requestHandler.handle(DEFAULT_MESSAGE_ID, new CertificateSignedRequest().withCert(stringToCiStringsList(validCertificate)));

        verify(stationMessageSenderMock).sendCallResult(any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getStatus().value()).isEqualTo(CertificateSignedResponse.Status.ACCEPTED.value());
        verify(stationStoreMock).setStationCertificate(any());



        ScheduledFuture scheduledFuture = requestHandler.getScheduledFuture();
        assertThat(Optional.ofNullable(scheduledFuture)).isNotEmpty();

        Instant triggerInstant = Instant.now().plusSeconds(scheduledFuture.getDelay(TimeUnit.SECONDS));
        ZonedDateTime triggerDate = ZonedDateTime.ofInstant(triggerInstant, ZoneId.systemDefault());
        assertThat(triggerDate.getDayOfMonth()).isEqualTo(5);
        assertThat(triggerDate.getMonthValue()).isEqualTo(11);
        assertThat(triggerDate.getYear()).isEqualTo(2029);
    }

    private List<CiString.CiString5500> stringToCiStringsList(String certificate) {
        List<CiString.CiString5500> result = new ArrayList<>();
        Splitter.fixedLength(5500).split(certificate).forEach(c -> result.add(new CiString.CiString5500(c)));
        return result;
    }

}