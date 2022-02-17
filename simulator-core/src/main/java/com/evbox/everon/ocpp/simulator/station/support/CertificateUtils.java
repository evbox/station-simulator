package com.evbox.everon.ocpp.simulator.station.support;

import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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

    public static boolean isCertificateValid(X509Certificate certificate, boolean isHardwareCertificate) {
        try {
            if(!isHardwareCertificate){
                certificate.checkValidity();
            }
            String serialCode = StringUtils.removeStart(certificate.getSubjectDN().getName(), "CN=");
            if (!StationHardwareData.SERIAL_NUMBER.equals(serialCode)) {
                return false;
            }
        } catch (Exception e) {
            log.debug("Exception while checking certificate validity", e);
            return false;
        }
        return true;
    }

    public static String loadCertificateChain(String path){
        StringBuilder certificateChain = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {

            String str;
            while ((str = reader.readLine()) != null) {
                certificateChain.append(str).append(System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return certificateChain.toString();
    }

    public static String generatePKCS10(PublicKey publicKey, PrivateKey privateKey) throws IOException, OperatorCreationException {
        X500Principal principal = new X500Principal( "CN=" + StationHardwareData.SERIAL_NUMBER);
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(principal, publicKey);
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNATURE_ALG);
        ContentSigner signer = csBuilder.build(privateKey);
        PKCS10CertificationRequest csr = p10Builder.build(signer);

        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(csr);
        pemWriter.close();
        stringWriter.close();
        return stringWriter.toString();
    }

    public static KeyPair generateKeyPair() {
        try{
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

    public static KeyPair loadKeyPair(String path)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException {
        // Read Public Key.
        File filePublicKey = new File(path + "/public.key");
        FileInputStream fis = new FileInputStream(path + "/public.key");
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();

        // Read Private Key.
        File filePrivateKey = new File(path + "/private.key");
        fis = new FileInputStream(path + "/private.key");
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        fis.close();

        // Generate KeyPair.
        Security.addProvider(new BouncyCastleProvider());
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

}
