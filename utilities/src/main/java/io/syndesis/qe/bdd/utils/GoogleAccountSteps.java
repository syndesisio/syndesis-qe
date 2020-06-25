package io.syndesis.qe.bdd.utils;

import io.cucumber.java.en.Given;
import io.syndesis.qe.utils.GoogleAccounts;
import org.springframework.beans.factory.annotation.Autowired;

public class GoogleAccountSteps {

    @Autowired
    private GoogleAccounts googleAccounts;

    @Given("^renew access token for \"([^\"]*)\" google account$")
    public void renewAccessTokenForAccount(String googleAccount) throws Throwable {
        googleAccounts.getGoogleAccountForTestAccount(googleAccount).renewAccessToken();
    }
}
