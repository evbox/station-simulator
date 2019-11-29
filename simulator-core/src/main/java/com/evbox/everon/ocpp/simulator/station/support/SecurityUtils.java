package com.evbox.everon.ocpp.simulator.station.support;

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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

public final class SecurityUtils {

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
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws PKCSException
     */
    public static PKCS12PfxPdu generateKeyStore(X509Certificate certificate, List<X509Certificate> chain, PublicKey publicKey, PrivateKey privateKey) throws NoSuchAlgorithmException, IOException, PKCSException {
        char[] emptyPassword = new char[0];

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        PKCS12SafeBagBuilder stationCertBagBuilder = new JcaPKCS12SafeBagBuilder(certificate);
        stationCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station's Key"));
        stationCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        if (chain == null) {
            chain = Collections.emptyList();
        }

        PKCS12SafeBag[] certs = new PKCS12SafeBag[chain.size() + 1];
        int index = 0;
        certs[index++] = stationCertBagBuilder.build();

        for (X509Certificate cert : chain) {
            PKCS12SafeBagBuilder caCertBagBuilder = new JcaPKCS12SafeBagBuilder(cert);
            caCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station Intermediate Certificate"));
            certs[index++] = caCertBagBuilder.build();
        }

        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privateKey, new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(emptyPassword));

        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Station's Key"));
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        // construct the actual key store
        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
        pfxPduBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, new CBCBlockCipher(new RC2Engine())).build(emptyPassword), certs);

        pfxPduBuilder.addData(keyBagBuilder.build());

        return pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), emptyPassword);
    }

}
