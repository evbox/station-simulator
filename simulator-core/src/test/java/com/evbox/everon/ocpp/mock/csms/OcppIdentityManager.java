package com.evbox.everon.ocpp.mock.csms;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.undertow.util.Headers.BASIC;

/**
 * Ocpp user authentication manager
 */
public class OcppIdentityManager {

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    private final Map<String, String> receivedCredentials = new ConcurrentHashMap<>();

    private final String username;
    private String password;

    OcppIdentityManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean verify(final String authorizationHeader) {

        byte[] decodedBase64 = Base64.getDecoder().decode(authorizationHeader.substring(PREFIX_LENGTH));
        String plainBase64 = new String(decodedBase64, StandardCharsets.UTF_8);

        int colIndex = plainBase64.indexOf(COLON);

        byte[] passwordBytes = Arrays.copyOfRange(decodedBase64, colIndex + 1, decodedBase64.length);

        String password = Hex.encodeHexString(passwordBytes);

        String username = plainBase64.substring(0, colIndex);
        receivedCredentials.put(username, password);

        return this.username.equals(username) && this.password.equals(password);

    }

    /**
     * Check whether credentials were received or not.
     *
     * @return `true` if received otherwise `false`
     */
    public boolean hasCredentials() {
        return !receivedCredentials.isEmpty();
    }

    /**
     * Getter for received user credentials.
     *
     * @return map of user credentials
     */
    public Map<String, String> getReceivedCredentials() {
        return receivedCredentials;
    }

    /**
     * Setter for password.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
