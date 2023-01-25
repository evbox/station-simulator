package com.evbox.everon.ocpp.simulator.station.support.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class PublicPrivateKeyUtils {
    private static final KeyFactory keyFactory;

    // Generate KeyPair.
    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair loadKeyPair(String path) {
        String privateKeyPath = path + "/private.key";
        PrivateKey privateKey = ThrowingSupplier.getAndSuppressException(() -> readPemPrivateKey(privateKeyPath))
                .orElseGet(() ->
                        ThrowingSupplier.getAndSuppressException(() -> getLegacyFormatPrivateKey(privateKeyPath))
                                .orElseThrow(() -> new RuntimeException("Private key cannot be loaded from " + privateKeyPath))
                );

        String publicKetPath = path + "/public.key";
        PublicKey publicKey = ThrowingSupplier.getAndSuppressException(() -> readPemPublicKey(publicKetPath))
                .orElseGet(() ->
                        ThrowingSupplier.getAndSuppressException(() -> getLegacyStagingFormatPublicKey(publicKetPath))
                                .orElseThrow(() -> new RuntimeException("Public key cannot be loaded from " + publicKetPath))
                );

        return new KeyPair(publicKey, privateKey);
    }

    public static PrivateKey readPemPrivateKey(String file) throws IOException, InvalidKeySpecException {
        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return keyFactory.generatePrivate(privKeySpec);
        }
    }

    public static PublicKey readPemPublicKey(String file) throws Exception {
        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return keyFactory.generatePublic(pubKeySpec);
        }
    }

    private static PublicKey getLegacyStagingFormatPublicKey(String path) throws IOException, InvalidKeySpecException {
        // Read Public Key.
        File filePublicKey = new File(path);
        byte[] encodedPublicKey;
        try (FileInputStream fis = new FileInputStream(path)) {
            encodedPublicKey = new byte[(int) filePublicKey.length()];
            fis.read(encodedPublicKey);
        }
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        return keyFactory.generatePublic(publicKeySpec);
    }

    private static PrivateKey getLegacyFormatPrivateKey(String path) throws IOException, InvalidKeySpecException {
        // Read Private Key.
        File filePrivateKey = new File(path);
        byte[] encodedPrivateKey;
        try (FileInputStream fis = new FileInputStream(path)) {
            encodedPrivateKey = new byte[(int) filePrivateKey.length()];
            fis.read(encodedPrivateKey);
        }
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        return keyFactory.generatePrivate(privateKeySpec);
    }

}
