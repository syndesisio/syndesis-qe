package io.syndesis.qe.steps.connections.wizard.phases;

import cucumber.api.java.en.When;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnection;

import java.util.Optional;

public class ConfigureConnectionSteps {

    private final ConfigureConnection configureConnectionPage = new ConfigureConnection();

    @When("^she fills \"([^\"]*)\" connection details$")
    public void fillConnectionDetails(String connectionName) {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(connectionName);

        if (optional.isPresent()) {
            // if connectionName credentials exist
            configureConnectionPage.fillForm(optional.get().getProperties());
        } else {
            String nameTransformed = connectionName.toLowerCase().replaceAll(" ", "_");
            optional = AccountsDirectory.getInstance().getAccount(nameTransformed);

            if (optional.isPresent()) {
                // if connectionName credentials exist with transformed name
                configureConnectionPage.fillForm(optional.get().getProperties());
            } else {
                if (!connectionName.equalsIgnoreCase("no credentials")) {
                    throw new IllegalArgumentException("Credentials for " + connectionName + " were not found!");
                }
            }
        }
    }
}
