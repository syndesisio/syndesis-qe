package io.syndesis.qe.steps.connections.wizard.phases;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;

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
        TestUtils.waitFor(() -> $$(By.tagName("form")).filter(cssClass("required-pf")).size() > 0,
            1, 20, "Form was not loaded in 20s");

        ElementsCollection formsCollection = $$(By.tagName("form")).filter(cssClass("required-pf"));
        assertThat(formsCollection).hasSize(1);

        new Form(formsCollection.get(0).shouldBe(visible)).fillByTestId(
            properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue))
        );
    }
}
