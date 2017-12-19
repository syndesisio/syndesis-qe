package io.syndesis.qe.steps.customizations.extensions;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.openqa.selenium.By;

import java.nio.file.Path;
import java.nio.file.Paths;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.customizations.CustomizationsPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsImportPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TechnicalExtensionSteps {
	
	private CustomizationsPage customizationsPage = new CustomizationsPage();
	private TechExtensionsImportPage techExtensionsImportPage = new TechExtensionsImportPage();
	private ModalDialogPage modalDialogPage = new ModalDialogPage();

	@Then("^she is presented with dialog page \"([^\"]*)\"$")
	public void isPresentedWithDialogPage(String title) throws Throwable {
		String titleText = new ModalDialogPage().getTitleText();
		assertThat(titleText.equals(title), is(true));
	}

	@When("^she clicks on the modal dialog \"([^\"]*)\" button$")
	public void clickModalButton(String buttonName) throws Exception {
		modalDialogPage.getButton(buttonName).shouldBe(visible).click();
    	
	}

	@When("^Camilla upload extension$")
	public void uploadFile() throws Throwable {
		//TODO temporary solution
		String techExtensionUrl = TestConfiguration.techExtensionUrl();
		Path techExtensionJar = Paths.get(techExtensionUrl).toAbsolutePath();
		$(By.cssSelector("input[type='file']")).shouldBe(visible).uploadFile(techExtensionJar.toFile());
	}

	@When("^she see details about imported extension$")
	public void importDetails() throws Throwable {
		//TODO Deeper validation
		assertThat(techExtensionsImportPage.validate(), is(true));
		
		techExtensionsImportPage.getButton("Import").shouldBe(visible);
		techExtensionsImportPage.getButton("Cancel").shouldBe(visible);
	}

	@Then("^technical extension \"([^\"]*)\" is present in technical extensions list$")
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
