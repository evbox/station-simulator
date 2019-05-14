package com.evbox.everon.ocpp.mock.csms;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ocpp user authentication manager
 */
public class OcppIdentityManager implements IdentityManager {

    private final String username;
    private final String password;
    private final Map<String, String> receivedCredentials = new ConcurrentHashMap<>();

    OcppIdentityManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     * @param account {@link Account}
     * @return {@link Account}
     */
    @Override
    public Account verify(Account account) {
        throw new UnsupportedOperationException();
    }

    /**
     * Verify username and password.
     *
     * @param username
     * @param credential
     * @return {@link Account} if authentication was successful otherwise null
     */
    @Override
    public Account verify(String username, Credential credential) {
        return getAccount(username, credential).orElse(null);
    }

    /**
     * {@inheritDoc}
     * @param credential {@link Credential}
     * @return {@link Account}
     */
    @Override
    public Account verify(Credential credential) {
        throw new UnsupportedOperationException();
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

    private Optional<Account> getAccount(String username, Credential credential) {
        String password = getPassword(credential);

        receivedCredentials.put(username, password);

        if (this.username.equals(username) && this.password.equals(password)) {

            return Optional.of(new Account() {

                @Override
                public Principal getPrincipal() {
                    return () -> username;
                }

                @Override
                public Set<String> getRoles() {
                    return Collections.emptySet();
                }

            });
        }

        return Optional.empty();
    }

    private String getPassword(Credential credential) {
        if (credential instanceof PasswordCredential) {
            return new String(((PasswordCredential) credential).getPassword());
        }
        throw new IllegalArgumentException("No password received");
    }

}
