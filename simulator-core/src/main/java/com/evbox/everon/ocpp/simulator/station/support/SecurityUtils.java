package com.evbox.everon.ocpp.simulator.station.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class SecurityUtils {

    private static final char[] EMPTY_PASSWORD = new char[0];

    private SecurityUtils() {
        // NOP
    }

    /**
     * Generates a PCKS12 trust store containing the specified certificates.
     *
     * @param certificate   station certificate
     * @param chain         certificates chain without leaf certificate
     * @param publicKey     station public key
     * @param privateKey    station private key
     * @return              pkcs12 trust store with arguments passed above
     */
    public static KeyStore generateKeyStore(X509Certificate certificate, List<X509Certificate> chain, PublicKey publicKey, PrivateKey privateKey) throws NoSuchAlgorithmException, IOException, PKCSException, KeyStoreException, CertificateException {
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        PKCS12SafeBagBuilder stationCertBagBuilder = new JcaPKCS12SafeBagBuilder(certificate);
        stationCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station's Key"));
        stationCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        if (chain == null) {
            chain = Collections.emptyList();
        }

        List<PKCS12SafeBag> certs = new ArrayList<>();
        certs.add(stationCertBagBuilder.build());
        chain.forEach(c -> {
            try {
                PKCS12SafeBagBuilder caCertBagBuilder = new JcaPKCS12SafeBagBuilder(c);
                caCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station Intermediate Certificate"));
                certs.add(caCertBagBuilder.build());
            } catch (IOException e) {
                log.error("Error while adding intermediate certificate to key store", e);
            }
        });

        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privateKey, new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(EMPTY_PASSWORD));

        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station's Key"));
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        // construct the actual key store
        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
        pfxPduBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, new CBCBlockCipher(new RC2Engine())).build(EMPTY_PASSWORD), certs.toArray(new PKCS12SafeBag[0]));

        pfxPduBuilder.addData(keyBagBuilder.build());
        PKCS12PfxPdu pfx = pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), EMPTY_PASSWORD);

        KeyStore trustStore  = KeyStore.getInstance("PKCS12");
        trustStore.load(new ByteArrayInputStream(pfx.getEncoded()), EMPTY_PASSWORD);

        return trustStore;
    }

    public static X509TrustManager createTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    public static SSLContext prepareSSLContext(KeyStore trustStore) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return SSLContexts.custom()
                .loadKeyMaterial(trustStore, new char[0])
                .build();
    }

}
