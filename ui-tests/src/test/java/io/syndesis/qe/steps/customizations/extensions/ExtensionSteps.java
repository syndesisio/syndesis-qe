package io.syndesis.qe.steps.customizations.extensions;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.customizations.CustomizationsPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsImportPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsListComponent;
import io.syndesis.qe.steps.CommonSteps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class ExtensionSteps {

    private CustomizationsPage customizationsPage = new CustomizationsPage();
    private TechExtensionsImportPage techExtensionsImportPage = new TechExtensionsImportPage();
    private TechExtensionsListComponent techExtensionsListComponent = new TechExtensionsListComponent();
    private ModalDialogPage modalDialogPage = new ModalDialogPage();

    private static final class NavigationElements {
        public static final String CUSTOMIZATIONS_NAV = "Customizations";
        public static final String EXTENSION_NAV = "Extensions";
        public static final String EXTENSION_DETAILS_NAV = "Extensions Details";

        public static final String IMPORT_EXTENSION_BUTTON = "Import Extension";
    }

    @Given("^import extensions$")
    public void importExtension(DataTable extensionsData) throws Throwable {
        CommonSteps commonSteps = new CommonSteps();

        List<List<String>> dataTable = extensionsData.raw();

        for (List<String> dataRow : dataTable) {

            commonSteps.navigateTo("", NavigationElements.CUSTOMIZATIONS_NAV);
            commonSteps.validatePage("", NavigationElements.CUSTOMIZATIONS_NAV);

            commonSteps.clickOnLink(NavigationElements.EXTENSION_NAV);
            commonSteps.validatePage("", NavigationElements.EXTENSION_NAV);

            commonSteps.clickOnButton(NavigationElements.IMPORT_EXTENSION_BUTTON);
            commonSteps.validatePage("", NavigationElements.IMPORT_EXTENSION_BUTTON);


            uploadExtensionFromFile(dataRow.get(0), dataRow.get(1));

            commonSteps.clickOnButton(NavigationElements.IMPORT_EXTENSION_BUTTON);
        }
    }

    @Given("^import extensions from syndesis-extensions folder$")
    public void importExtensionFromKnownFolder(DataTable extensionsData) throws Throwable {
        CommonSteps commonSteps = new CommonSteps();

        List<List<String>> dataTable = extensionsData.raw();

        for (List<String> dataRow : dataTable) {

            commonSteps.navigateTo("", NavigationElements.CUSTOMIZATIONS_NAV);
            commonSteps.validatePage("", NavigationElements.CUSTOMIZATIONS_NAV);

            commonSteps.clickOnLink(NavigationElements.EXTENSION_NAV);
            commonSteps.validatePage("", NavigationElements.EXTENSION_NAV);

            commonSteps.clickOnButton(NavigationElements.IMPORT_EXTENSION_BUTTON);
            commonSteps.validatePage("", NavigationElements.IMPORT_EXTENSION_BUTTON);


            uploadExtensionFromFile(dataRow.get(0));

            commonSteps.clickOnButton(NavigationElements.IMPORT_EXTENSION_BUTTON);
        }
    }


    @When("^.*uploads? extension \"([^\"]*)\"$")
    public void uploadFile(String extensionName) throws Throwable {
        uploadExtensionFromFile(extensionName);
    }

    /**
     * Uploads an extension from any relative path
     *
     * @param extensionName
     * @param extensionPath
     * @throws Throwable
     */
    @When("^.*uploads? extension with name \"([^\"]*)\" from relative path \"([^\"]*)\"$")
    public void uploadExtensionFromFile(String extensionName, String extensionPath) throws Throwable {

        String techExtensionUrl = extensionPath + extensionName;
        Path techExtensionJar = Paths.get(techExtensionUrl).toAbsolutePath();
        $(By.cssSelector("input[type='file']")).shouldBe(visible).uploadFile(techExtensionJar.toFile());
    }


    /**
     * Method will download extension .jar file from ../syndesis-extensions/extensionFolderName/target/*.jar
     * You only have to specify folder name.
     *
     * @param extensionFolderName
     * @throws Throwable
     */
    @When("^.*uploads? extension with name \"([^\"]*)\" from syndesis-extensions dir$")
    public void uploadExtensionFromFile(String extensionFolderName) throws Throwable {
        String defaultPath = "../syndesis-extensions/" + extensionFolderName + "/target/";

        File[] files = new File(defaultPath).listFiles((dir, name) -> !name.contains("original") && name.endsWith(".jar"));
        Assertions.assertThat(files).hasSize(1).doesNotContainNull();

        String techExtensionUrl = defaultPath + files[0].getName();

        Path techExtensionJar = Paths.get(techExtensionUrl).toAbsolutePath();
        $(By.cssSelector("input[type='file']")).shouldBe(visible).uploadFile(techExtensionJar.toFile());
    }

    @When("^.*sees? details about imported extension$")
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
