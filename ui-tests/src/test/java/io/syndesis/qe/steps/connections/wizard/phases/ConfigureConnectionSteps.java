package io.syndesis.qe.steps.connections.wizard.phases;

import cucumber.api.java.en.When;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnection;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.Optional;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class ConfigureConnectionSteps {

    private final ConfigureConnection configureConnectionPage = new ConfigureConnection();

    @When("^she fills \"([^\"]*)\" connection details$")
    public void fillConnectionDetails(String connectionName) {
        doFillConnectionDetails(connectionName, false);
    }

    @When("^she fills \"([^\"]*)\" connection details from connection edit page$")
    public void fillConnectionDetailsFromConnectionEditPage(String connectionName) {
        doFillConnectionDetails(connectionName, true);
    }

    private void doFillConnectionDetails(String connectionName, boolean isEditPage) {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(connectionName);

        if (optional.isPresent()) {
            // if connectionName credentials exist
            callCorrectFormFill(optional.get().getProperties(), isEditPage);
        } else {
            String nameTransformed = connectionName.toLowerCase().replaceAll(" ", "_");
            optional = AccountsDirectory.getInstance().getAccount(nameTransformed);

            if (optional.isPresent()) {
                // if connectionName credentials exist with transformed name
                callCorrectFormFill(optional.get().getProperties(), isEditPage);
            } else {
                if (!connectionName.equalsIgnoreCase("no credentials")) {
                    throw new IllegalArgumentException("Credentials for " + connectionName + " were not found!");
                }
            }
        }
    }

    private void callCorrectFormFill(Map<String, String> properties, boolean isEditPage) {
        if (isEditPage) {
            new Form($(By.cssSelector("syndesis-connection-detail-configuration")).shouldBe(visible)).fillById(properties);
        } else {
            configureConnectionPage.fillForm(properties);
        }
    }
}
