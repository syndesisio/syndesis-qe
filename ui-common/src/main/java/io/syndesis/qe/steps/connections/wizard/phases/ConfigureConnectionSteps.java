package io.syndesis.qe.steps.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.cucumber.java.en.When;

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
        SelenideElement form = $(By.className("pf-c-form")).shouldBe(visible, Duration.ofSeconds(5));

        new Form(form.shouldBe(visible)).fillByTestId(
            properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue))
        );
    }

    public ElementsCollection getAllToolTips() {
        return $$(ByUtils.dataTestId("tooltip"));
    }
}
