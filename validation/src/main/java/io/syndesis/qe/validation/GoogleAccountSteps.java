package io.syndesis.qe.validation;

import io.syndesis.qe.utils.google.GoogleAccounts;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;

public class GoogleAccountSteps {

    @Autowired
    private GoogleAccounts googleAccounts;

    @Given("^renew access token for \"([^\"]*)\" google account$")
    public void renewAccessTokenForAccount(String googleAccount) throws Throwable {
        googleAccounts.getGoogleAccountForTestAccount(googleAccount).renewAccessToken();
    }
}
