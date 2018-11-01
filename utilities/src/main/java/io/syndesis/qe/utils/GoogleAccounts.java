package io.syndesis.qe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

/**
 * This class defines Google accounts available for the test suite. Each account instance is annotated by a @Bean annotation
 * which assures, that the instance is reused across whole suite. The reason for this is that the GoogleAccount instance
 * holds the access_token and creating multiple instances for a single account would cause reissuing access_token possibly
 * leading to invalidation of the previously created ones.
 */
@Configuration
@Lazy
public class GoogleAccounts {

    @Autowired
    private ApplicationContext context;

    public GoogleAccount getGoogleAccountForTestAccount(String testAccount) {
        return context.getBean(testAccount, GoogleAccount.class);
    }

    @Bean(name="QE Google Mail")
    public GoogleAccount qeGoogleAccount() throws IOException {
        GoogleAccount googleAccount = new GoogleAccount("QE Google Mail");
        googleAccount.renewAccessToken();
        return googleAccount;
    }

    @Bean(name="QE Google Calendar")
    public GoogleAccount qeGoogleCalendarAccount() throws IOException {
        GoogleAccount googleAccount = new GoogleAccount("QE Google Calendar");
        googleAccount.renewAccessToken();
        return googleAccount;
    }
}
