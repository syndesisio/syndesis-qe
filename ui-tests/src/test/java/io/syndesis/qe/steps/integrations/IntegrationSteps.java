package io.syndesis.qe.steps.integrations;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.client.utils.Utils;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.integrations.detail.IntegrationDetailPage;
import io.syndesis.qe.pages.integrations.detail.IntegrationDetailPageFactory;
import io.syndesis.qe.pages.integrations.edit.ActionConfigureComponent;
import io.syndesis.qe.pages.integrations.edit.ConnectionSelectComponent;
import io.syndesis.qe.pages.integrations.edit.FlowViewComponent;
import io.syndesis.qe.pages.integrations.edit.IntegrationAddStepPage;
import io.syndesis.qe.pages.integrations.edit.IntegrationEditPage;
import io.syndesis.qe.pages.integrations.edit.ListActionsComponent;
import io.syndesis.qe.pages.integrations.edit.TwitterSearchActionConfigureComponent;
import io.syndesis.qe.pages.integrations.edit.steps.BasicFilterStepPage;
import io.syndesis.qe.pages.integrations.edit.steps.StepFactory;
import io.syndesis.qe.pages.integrations.edit.steps.StepPage;
import io.syndesis.qe.pages.integrations.list.IntegrationsListComponent;
import io.syndesis.qe.pages.integrations.list.IntegrationsListPage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class IntegrationSteps {

	//FACTORIES:
	private StepFactory stepFactory = new StepFactory();
	private IntegrationDetailPageFactory detailPageFactory = new IntegrationDetailPageFactory();
	//PAGES:
	private SyndesisRootPage rootPage = new SyndesisRootPage();
	private IntegrationEditPage editPage = new IntegrationEditPage();
	private IntegrationDetailPage detailPage = new IntegrationDetailPage();
	private IntegrationsListPage listPage = new IntegrationsListPage();
	private IntegrationAddStepPage addStepPage = new IntegrationAddStepPage();
	//PAGE COMPONENTS:
	//TODO(sveres): unify access to Components objects through Page objects strictly
	private FlowViewComponent flowViewComponent = new FlowViewComponent();
	private ConnectionSelectComponent selectComponent = new ConnectionSelectComponent();
	private IntegrationsListComponent listComponent = new IntegrationsListComponent();
	private ListActionsComponent listActions = new ListActionsComponent();
	private TwitterSearchActionConfigureComponent twitterSearchComponent = new TwitterSearchActionConfigureComponent();

	@When("defines integration name \"(\\w+)\"$")
	public void defineIntegrationName(String integrationName) {
		editPage.basicsComponent().setName(integrationName);
	}

	@Then("^she is presented with a visual integration editor$")
	public void editorOpened() {
		editPage.getRootElement().shouldBe(visible);
		selectComponent.getRootElement().shouldBe(visible);
		flowViewComponent.getRootElement().shouldBe(visible);
	}

	@Then("^she is presented with a visual integration editor for \"(\\w+)\"$")
	public void editorOpenedFor(String integrationName) {
		this.editorOpened();
		log.info("editor must display integration name {}", integrationName);
		assertThat(editPage.flowViewComponent().getIntegrationName(), is(integrationName));
	}

	@Then("^Camilla is presented with \"(\\w+)\" integration details$")
	public void verifyIntegrationDetails(String integrationName) {
		log.info("Integration detail editPage must show integration name");
		assertThat(detailPage.getIntegrationName(), is(integrationName));
	}

	@When("^Camilla selects the \"(\\w+)\" integration.*$")
	public void selectConnection(String itegrationName) {
		listPage.listComponent().goToIntegrationDetail(itegrationName);
	}

	@When("^she selects \"(\\w+)\" integration action$")
	public void selectIntegrationAction(String action) {
		if ("Create Opportunity".equals(action)) {
			log.warn("Action {} is not available", action);
			listActions.selectAction("Create Salesforce object");
		}
		listActions.selectAction(action);
	}

	@When("^Camilla deletes the \"(\\w+)\" integration*$")
	public void deleteIntegration(String integrationName) {
		listComponent.clickDeleteIntegration(integrationName);
	}

	@When("^Camilla deletes the integration on detail page*$")
	public void deleteIntegrationOnDetailPage() {
		detailPage.deleteIntegration();
	}

	@Then("^she can see on detail editPage that integration is \"(\\w+)\" status$")
	public void checkStatus(String expectedStatus) {
		String status = detailPage.getStatus();
		log.info("Status: {}", status);
		assertThat(expectedStatus, is(status));
	}

	@Then("^she clicks on integration in \"(\\w+)\" status and check on detail if status match and appropriate actions are available$")
	public void clickOnIntegrationInStatus(String status) {
		SelenideElement integrationByStatus = listComponent.getIntegrationByStatus(status);
		integrationByStatus.shouldBe(visible).click();
		IntegrationDetailPage detailPageSpecific = detailPageFactory.getDetailPage(status);
		for (String action : detailPageSpecific.actionsSet) {
			log.info("Action: {}", action);
			SelenideElement actionButton = detailPageSpecific.getActionButton(action);
			log.info("There should by button for {} action on {} status", action, status);
			actionButton.shouldBe(visible);
			log.info("Status on detail editPage should be equal to expected status");
			assertThat(detailPageSpecific.getStatus(), is(status));
		}
		detailPageSpecific.getActionButton("Done").shouldBe(visible).click();
	}

	@Then("^she go trough whole list of integrations and check on detail if status match and appropriate actions are available$")
	public void goTrouhListAndCheckDetails() {
		ElementsCollection integrations = listComponent.getAllIntegrations();

		for (SelenideElement integration : integrations) {
			String status = listComponent.getIntegrationItemStatus(integration);
			log.info("Status: {}", status);
			integration.shouldBe(visible).click();
			IntegrationDetailPage detailPageSpecific = detailPageFactory.getDetailPage(status);
			for (String action : detailPageSpecific.actionsSet) {
				log.info("Action: {}", action);
				log.info("There should by button for {} action on {} status", action, status);
				SelenideElement actionButton = detailPageSpecific.getActionButton(action).shouldBe(visible);
			}
			log.info("Status on detail editPage should be equal to expected status:");
			assertThat(detailPageSpecific.getStatus(), is(status));
			detailPageSpecific.done();
		}
	}

	@When("^she selects \"(\\w+)\" integration step$")
	public void addStep(String stepName) {
		log.info("Adding {} step to integration", stepName);
		addStepPage.addStep(stepName);
	}

	@Then("^Integration \"(\\w+)\" is present in integrations list$")
	public void expectIntegrationPresent(String name) {
		log.info("Verifying integration {} is present", name);
		assertThat(listPage.listComponent().isIntegrationPresent(name), is(true));
	}

	@Then("^Camilla can not see \"(\\w+)\" integration anymore$")
	public void expectIntegrationNotPresent(String name) {
		log.info("Verifying if integration {} is present", name);
		assertThat(listPage.listComponent().isIntegrationPresent(name), is(false));
	}

	@Then("^she wait until integration \"(\\w+)\" get into \"(\\w+)\" state$")
	public void waitForIntegrationState(String integrationName, String integrationState) {
		SelenideElement integrationActiveState = listPage.listComponent().getIntegrationActiveState(integrationName, integrationState);
		log.info("Integration should get into {}.", integrationState);
		integrationActiveState.shouldBe(visible);
	}

	@Then("^she is presented with a add step page$")
	public void addStepPageOpened() {
		log.info("there must be add step page root element");
		addStepPage.getRootElement().shouldBe(visible);
	}

	@Then("^she is presented with a \"(\\w+)\" step configure page$")
	public void configureStepPageOpen(String stepType) {
		StepPage page = stepFactory.getStep(stepType, "");
		log.info("there must be add step editPage root element");
		page.getRootElement().shouldBe(visible);
		assertThat(page.validate(), is(true));
	}

	@Then("^she fill configure page for \"(\\w+)\" step with \"(\\w+)\" parameter$")
	public void fillStepConfiguration(String stepType, String parameter) {
		StepPage page = stepFactory.getStep(stepType, parameter);
		page.fillConfiguration();
	}

	@Then("^she adds \"(\\d+)\" random steps and then check the structure$")
	public void addRandomStepsAndCheckRest(Integer numberOfSteps) {
		log.info("Adding random steps");
		List<String> list = flowViewComponent.getStepsArray();
		editPage.clickButton("Add a Step");
		ElementsCollection links = editPage.getLinks("Add a step");
		Integer count = links.size();
		List<Integer> randomIndexes = new ArrayList<>();
		for (int i = 0; i < numberOfSteps; i++) {
			randomIndexes.add((int) Math.floor((Math.random() * count)));
		}
		for (int randomIndex : randomIndexes) {
			links.get(randomIndex).click();
			String stepType = "Basic Filter";
			String stepParameter = "ANY of the following, pathx " + randomIndex + ", Contains, valuex " + randomIndex;
			addStepPage.addStep(stepType);
			StepPage stepPage = stepFactory.getStep(stepType, stepParameter);
			stepPage.fillConfiguration();
			editPage.clickButton("Next");
			editPage.clickButton("Add a Step");
			list.add(randomIndex, stepParameter);
		}
		List<String> list2 = flowViewComponent.getStepsArray();
		for (int i = 0; i < list2.size(); i++) {
			log.info("assserting {} and {}", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she delete \"(\\d+)\" random steps and check rest$")
	public void deleteRandomStepsAndCheckRest(Integer numberOfSteps) {
		log.info("Deleting random steps");
		List<String> list = flowViewComponent.getStepsArray();
		ElementsCollection deletes = editPage.getAllDeletes().shouldBe(sizeGreaterThanOrEqual(1));
		int count = deletes.size();
		List<Integer> randomIndexes = new ArrayList<>();
		for (int i = 0; i < numberOfSteps; i++) {
			randomIndexes.add((int) Math.floor(Math.random() * (count - 2 - i)));
		}
		for (Integer randomIndex : randomIndexes) {
			deletes.get(randomIndex + 1).click();
			editPage.clickOnFirstVisibleButton("OK");
			list.remove(randomIndex);
		}
		List<String> list2 = flowViewComponent.getStepsArray();
		for (int i = 0; i < list.size(); i++) {
			log.info("assserting {} and {", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she delete step on position \"(\\d+)\" and check rest$")
	public void deleteStepOnPositionAndCheckRest(Integer positionOfStep) {
		log.info("Deleting step on position {}", positionOfStep);
		List<String> list = flowViewComponent.getStepsArray();
		ElementsCollection deletes = this.editPage.getAllDeletes().shouldBe(sizeGreaterThanOrEqual(1));
		Integer indexOfStep = positionOfStep + 1;
		deletes.get(indexOfStep).click();
		editPage.clickOnFirstVisibleButton("OK");
		list.remove(positionOfStep);
		//NOW CHECK:
		List<String> list2 = flowViewComponent.getStepsArray();
		for (int i = 0; i < list.size(); i++) {
			log.info("assserting {} and {}", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she is presented with an actions list$")
	public void expectActionListIsPresent() {
		log.info("There must be action list loaded");
		listActions.getRootElement().shouldBe(visible);
	}

	@Then("^add new basic filter rule with \"(\\w+)\" parameters$")
	public void addBasicFilterRule(String rule) {
		BasicFilterStepPage basicFilterStepPage = (BasicFilterStepPage) stepFactory.getStep("BASIC FILTER", "");
		basicFilterStepPage.initialize();
		basicFilterStepPage.addRule(rule);
	}

	@Then("^delete \"(\\d+)\" random basic filter rule$")
	public void deleteRandomFilterRules(Integer numberOfRules) {
		for (int i = 0; i < numberOfRules; i++) {
			editPage.clickRandomTrash();
		}
	}

	@Then("^delete basic filter rule on position \"(\\d+)\"$")
	public void deleteFilterRuleOnPosition(Integer position) {
		ElementsCollection trashes = editPage.getAllTrashes();
		trashes.get(position - 1).click();
	}

	//Kebab menu test, #553 -> part #548, #549.
	@When("^clicks on the kebab menu icon of each available Integration and checks whether menu is visible and has appropriate actions$")
	public void clickOnAllKebabMenus() {
		IntegrationsListComponent integrationsListComponent = new IntegrationsListComponent();
		integrationsListComponent.checkAllIntegrationsKebabButtons();
	}

	// Twitter search specification
	@Then("^she fills keywords field with random text to configure search action$")
	public void fillKeywords() {
		String value = Utils.randomString(20);
		twitterSearchComponent.fillKeywordsValue(value);
	}

	@Then("^she fills \"(\\w+)\" action configure component input with \"(\\w+)\" value$")
	public void fillActionConfigureField(String fieldId, String value) {
		ActionConfigureComponent actionConfComponent = editPage.actionConfigureComponent();
		log.info("Input skould be visible");
		actionConfComponent.fillInput(fieldId, value);
	}

	/**
	 * whether it's start or finish connection
	 *
	 * @param type
	 */
	@Then("^she is prompted to select a \"(\\w+)\" connection from a list of available connections$")
	public void verifyTypeOfConnection(String type) {
		SelenideElement connection = editPage.flowViewComponent().flowConnection(type).getElement().shouldBe(visible);
		log.info("{} connection must be active", type);
	}

	/**
	 * check you are on the correct editPage
	 *
	 * @param text
	 */
	@Then("^she is presented with \"(\\w+)\" (page|editor)$")
	public void verifyPageByText(String text) {
		editPage.checkPageIsPresent(text);
	}
}
