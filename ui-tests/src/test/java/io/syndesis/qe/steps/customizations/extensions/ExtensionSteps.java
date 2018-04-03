package io.syndesis.qe.steps.customizations.extensions;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.customizations.CustomizationsPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsImportPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsListComponent;
import io.syndesis.qe.steps.CommonSteps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtensionSteps {

    private CustomizationsPage customizationsPage = new CustomizationsPage();
    private TechExtensionsImportPage techExtensionsImportPage = new TechExtensionsImportPage();
    private TechExtensionsListComponent techExtensionsListComponent = new TechExtensionsListComponent();
    private ModalDialogPage modalDialogPage = new ModalDialogPage();

    @Given("^imported extensions$")
    public void importExtension(DataTable extensionsData) throws Throwable {
        CommonSteps commonSteps = new CommonSteps();

        String extensionsLink = "Extensions";
        String importButton = "ImportIntegration Extension";

        List<List<String>> dataTable = extensionsData.raw();

        for (List<String> dataRow : dataTable) {
            String extensionName = dataRow.get(0);
            String extensionFileName = dataRow.get(1);

            commonSteps.navigateTo("", "Customizations");
            commonSteps.validatePage("", "Customizations");

            commonSteps.clickOnLink(extensionsLink);
            commonSteps.validatePage("", "Extensions");

            SelenideElement techExtensionItem = techExtensionsListComponent.getExtensionItem(extensionName);

            if (techExtensionItem != null) {
                log.warn("Extension {} already exists!", extensionName);
            } else {
                commonSteps.clickOnButton(importButton);
                commonSteps.validatePage("", "ImportIntegration Extension");

                uploadFile(extensionFileName);
                importDetails();

                commonSteps.clickOnButton(importButton);
                commonSteps.validatePage("", "Extension Details");
            }
        }
    }

    @When("^Camilla upload extension \"([^\"]*)\"$")
    public void uploadFile(String extensionName) throws Throwable {
        //TODO temporary solution
        String techExtensionFolderUrl = TestConfiguration.techExtensionUrl();
        String techExtensionUrl = techExtensionFolderUrl + extensionName + ".jar";
        Path techExtensionJar = Paths.get(techExtensionUrl).toAbsolutePath();
        $(By.cssSelector("input[type='file']")).shouldBe(visible).uploadFile(techExtensionJar.toFile());
    }

    @When("^she see details about imported extension$")
    public void importDetails() throws Throwable {
        //TODO Deeper validation
        assertThat(techExtensionsImportPage.validate(), is(true));
    }

    @Then("^extension \"([^\"]*)\" is present in list$")
    public void expectExtensionPresent(String name) throws Throwable {
        log.info("Verifying if extension {} is present", name);
        assertThat(customizationsPage.getTechExtensionsListComponent().isExtensionPresent(name), is(true));
    }

    @Then("^Camilla can not see \"([^\"]*)\" technical extension anymore$")
    public void expectExtensionNonPresent(String name) throws Throwable {
        log.info("Verifying if extension {} is present", name);
        customizationsPage.getTechExtensionsListComponent().getExtensionItem(name).shouldNotBe(visible);
        assertThat(customizationsPage.getTechExtensionsListComponent().isExtensionPresent(name), is(false));
    }

    @Then("^Camilla choose \"([^\"]*)\" action on \"([^\"]*)\" technical extension$")
    public void chooseActionOnTechExtensionItem(String action, String extensionName) throws Throwable {
        customizationsPage.getTechExtensionsListComponent().chooseActionOnExtension(extensionName, action);
    }

    @Then("^she can review uploaded technical extension$")
    public void techExtensionNewStepReview() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^she can see \"([^\"]*)\" in step list$")
    public void stepPresentInlist(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^she can see notification about integrations \"([^\"]*)\" in which is tech extension used$")
    public void usedInIntegrations(String integrations) throws Throwable {
        String[] integrationsNames = integrations.split(", ");

        for (String integrationName : integrationsNames) {
            modalDialogPage.getElementContainingText(By.cssSelector("strong"), integrationName).shouldBe(visible);
        }
    }
}
