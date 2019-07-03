package io.syndesis.qe.steps.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cucumber.api.java.en.When;

public class ConfigureConnectionSteps {

    @When("^fill in \"([^\"]*)\" connection details$")
    public void fillConnectionDetails(String connectionName) {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(connectionName);

        if (optional.isPresent()) {
            // if connectionName credentials exist
            fillFormInLowerCase(optional.get().getProperties());
        } else {
            String nameTransformed = connectionName.toLowerCase().replaceAll(" ", "_");
            optional = AccountsDirectory.getInstance().getAccount(nameTransformed);

            if (optional.isPresent()) {
                // if connectionName credentials exist with transformed name
                fillFormInLowerCase(optional.get().getProperties());
            } else {
                if (!"no credentials".equalsIgnoreCase(connectionName)) {
                    throw new IllegalArgumentException("Credentials for " + connectionName + " were not found!");
                }
            }
        }
    }

    private void fillFormInLowerCase(Map<String, String> properties) {
        //transfer all keys in properties to lower case
        new Form($("form .row-cards-pf").shouldBe(visible)).fillByTestId(
            properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue))
        );
    }
}
