package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

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
            int retries = 0;
            int timeoutInMinutes;
            while (retries < 4) {
                try {
                    salesforce = new ForceApi(new ApiConfig()
                        .setClientId(salesforceAccount.getProperty("clientId"))
                        .setClientSecret(salesforceAccount.getProperty("clientSecret"))
                        .setUsername(salesforceAccount.getProperty("userName"))
                        .setPassword(salesforceAccount.getProperty("password"))
                        .setForceURL(salesforceAccount.getProperty("loginUrl")));
                    return salesforce;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    timeoutInMinutes = ++retries;
                    log.error("Unable to connect to salesforce, will retry in {} minutes.", timeoutInMinutes);
                    TestUtils.sleepIgnoreInterrupt(timeoutInMinutes * 60000L);
                }
            }
            fail("Unable to connect to SalesForce");
        }
        return salesforce;
    }
}
