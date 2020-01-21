package io.syndesis.qe.utils;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SalesforceAccount {
    private static ForceApi salesforce;

    public static ForceApi getInstance() {
        if (salesforce == null) {
            final Account salesforceAccount = AccountsDirectory.getInstance().getAccount(Account.Name.SALESFORCE).get();
            TestUtils.withRetry(() -> {
                try {
                    salesforce = new ForceApi(new ApiConfig()
                        .setClientId(salesforceAccount.getProperty("clientId"))
                        .setClientSecret(salesforceAccount.getProperty("clientSecret"))
                        .setUsername(salesforceAccount.getProperty("userName"))
                        .setPassword(salesforceAccount.getProperty("password"))
                        .setForceURL(salesforceAccount.getProperty("loginUrl")));
                    return true;
                } catch (Exception ex) {
                    log.error("Unable to connect to salesforce, will retry in 5 minutes");
                    return false;
                }
            }, 3, 300000L, "Unable to connect to SalesForce");
        }
        return salesforce;
    }
}
