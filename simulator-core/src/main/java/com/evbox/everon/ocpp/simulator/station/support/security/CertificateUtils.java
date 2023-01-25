package com.evbox.everon.ocpp.simulator.station.support.security;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class CertificateUtils {

    private static final String SIGNATURE_ALG = "SHA256WITHECDSA";

    public static List<X509Certificate> convertStringToCertificates(String chain) {
        try {
            byte[] bytes = chain.getBytes();
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(bytes);
            return factory.generateCertificates(in).stream().map(certificate -> ((X509Certificate) certificate)).collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("Invalid certificate", e);
        }
        return Collections.emptyList();
    }

    public static boolean isCertificateValid(X509Certificate certificate, boolean isHardwareCertificate, String stationSerialNumber) {
        try {
            if (!isHardwareCertificate) {
                certificate.checkValidity();
            }
            if (!certificate.getSubjectDN().getName().contains(stationSerialNumber)) {
                return false;
            }
        } catch (Exception e) {
            log.debug("Exception while checking certificate validity", e);
            return false;
        }
        return true;
    }

    public static String loadCertificateChain(String path) {
        StringBuilder certificateChain = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            certificateChain.append(reader.lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return certificateChain.toString();
    }

    public static String generatePKCS10(PublicKey publicKey, PrivateKey privateKey, String stationSerialNumber) throws IOException, OperatorCreationException {
        X500Principal principal = new X500Principal("CN=" + stationSerialNumber);
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(principal, publicKey);
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNATURE_ALG);
        ContentSigner signer = csBuilder.build(privateKey);
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        try (StringWriter stringWriter = new StringWriter()) {
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
                pemWriter.writeObject(csr);
            }
            return stringWriter.toString();
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            Security.addProvider(new BouncyCastleProvider());

            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecSpec, new SecureRandom());
            return g.generateKeyPair();
        } catch (Exception e) {
            log.debug("Error while generating the key pair", e);
            return null;
        }
    }
}
