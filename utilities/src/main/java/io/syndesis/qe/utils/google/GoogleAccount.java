package io.syndesis.qe.utils.google;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Google Account representation, holds all important info got from the credentials.json.
 * It also holds the access_token generated explicitly. That's why there has to be only
 * a single instance of this class for a particular account definition. Otherwise there could
 * come issues with previously created access_tokens being invalidated.
 */
@Slf4j
public class GoogleAccount {

    private Account account;
    private Credential credential;
    private Calendar calendarClient;
    private Gmail gmailClient;
    private Sheets sheetsClient;

    public GoogleAccount(String accountName) {
        account = AccountsDirectory.getInstance().get(accountName);
        credential = createGoogleCredential(account);
    }

    /**
     * Method to create a google credential from given testing account.
     *
     * @param account id of testing account to use
     * @return Credential with client-id, client-secret, refresh-token and access-token
     */
    private static Credential createGoogleCredential(Account account) {

        Map<String, String> accountProps = account.getProperties();
        String clientId = accountProps.get("desktopClientId");
        String clientSecret = accountProps.get("desktopClientSecret");
        String refreshToken = accountProps.get("refreshToken");

        Credential credential = new GoogleCredential.Builder()
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setTransport(new NetHttpTransport())
                .setClientSecrets(clientId, clientSecret)
                .build();
        credential.setRefreshToken(refreshToken);
        return credential;
    }

    public void renewAccessToken() throws IOException {
        credential.refreshToken();
    }

    /**
     * Get credential of the Google Account.
     *
     * @return credential
     */
    public Credential getCredential() {
        return credential;
    }

    /**
     * Get a calendar client for this Google Account.
     *
     * @return Calendar client instance
     */
    public Calendar calendar() {
        if (calendarClient == null) {
            calendarClient = new Calendar.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                    .setApplicationName(account.getProperty("applicationName"))
                    .build();
        }
        return calendarClient;
    }

    /**
     * Get a gmail client for this Google Account.
     *
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

    public Sheets sheets() {
        if (sheetsClient == null) {
            sheetsClient = new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                    .setApplicationName(account.getProperty("applicationName"))
                    .build();
        }
        return sheetsClient;
    }
}
