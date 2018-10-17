package io.syndesis.qe.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.gmail.Gmail;
import io.syndesis.qe.accounts.Account;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;


import static org.assertj.core.api.Assertions.fail;

/**
 * Google Account representation, holds all important info got from the credentials.json.
 * It also holds the access_token generated on creation. That's why there has to be only
 * a single instance of this class for a particular account definition. Otherwise there could
 * come issues with previously created access_tokens being invalidated.
 */
@Slf4j
public class GoogleAccount {

    private Account account;
    private Credential credential;
    private Calendar calendarClient;
    private Gmail gmailClient;

    public GoogleAccount(String accountName) {
        account = AccountUtils.get(accountName);
        credential = createGoogleCredential(account);
    }

    /**
     * Method to generate an access token for the provided arguments.
     * @param clientId client-id of the configured application
     * @param clientSecret client-secret for the client-id
     * @param refreshToken refresh-token issued by google auth provider
     * @return TokenResponse with access-token set as its field
     */
    private static TokenResponse generateAccessToken(String clientId, String clientSecret, String refreshToken) {
        TokenResponse response = null;
        try {
            response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    refreshToken,
                    clientId,
                    clientSecret)
                    .execute();
        } catch (IOException e) {
            log.error("Access token error", e);
            fail("Could not generate access token.", e);
        }
        return response;
    }

    /**
     * Method to create a google credential from given testing account.
     * @param account id of testing account to use
     * @return Credential with client-id, client-secret, refresh-token and access-token
     */
    private static Credential createGoogleCredential(Account account) {

        Map<String, String> accountProps = account.getProperties();
        String clientId = accountProps.get("clientId");
        String clientSecret = accountProps.get("clientSecret");
        String refreshToken = accountProps.get("refreshToken");

        Credential credential = new GoogleCredential.Builder()
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setTransport(new NetHttpTransport())
                .setClientSecrets(clientId, clientSecret)
                .build();
        TokenResponse response = generateAccessToken(clientId, clientSecret, refreshToken);
        credential.setAccessToken(response.getAccessToken());
        return credential;
    }

    /**
     * Get credential of the Google Account.
     * @return
     */
    public Credential getCredential() {
        return credential;
    }

    /**
     * Get a calendar client for this Google Account.
     * @return Calendar client instance
     */
    public Calendar calendar() {
        if (calendarClient==null) {
            calendarClient = new Calendar.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                    .setApplicationName(account.getProperty("applicationName"))
                    .build();
        }
        return calendarClient;
    }

    /**
     * Get a gmail client for this Google Account.
     * @return Gmail client instance
     */
    public Gmail gmail() {
        if (gmailClient == null) {
            gmailClient = new Gmail.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                    .setApplicationName(account.getProperty("applicationName"))
                    .build();
        }
        return gmailClient;
    }

}
