package io.syndesis.qe.steps.integrations;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.client.utils.Utils;
import io.syndesis.qe.pages.integrations.detail.IntegrationDetailPage;
import io.syndesis.qe.pages.integrations.edit.IntegrationEditPage;
import io.syndesis.qe.pages.integrations.edit.steps.BasicFilterStepComponent;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponent;
import io.syndesis.qe.pages.integrations.list.IntegrationsListPage;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class IntegrationSteps {

	private IntegrationEditPage editPage = new IntegrationEditPage();
	private IntegrationDetailPage detailPage = new IntegrationDetailPage();
	private IntegrationsListPage listPage = new IntegrationsListPage();

	@When("^she defines integration name \"([^\"]*)\"$")
	public void defineIntegrationName(String integrationName) {
		editPage.getIntegrationBasicsComponent().setName(integrationName);
	}

	@Then("^she is presented with a visual integration editor$")
	public void editorOpened() {
		editPage.getRootElement().shouldBe(visible);
		editPage.getIntegrationConnectionSelectComponent().getRootElement().shouldBe(visible);
		editPage.getFlowViewComponent().getRootElement().shouldBe(visible);
	}

	@Then("^she is presented with a visual integration editor for \"([^\"]*)\"$")
	public void editorOpenedFor(String integrationName) {
		this.editorOpened();
		log.info("editor must display integration name {}", integrationName);
		assertThat(editPage.getFlowViewComponent().getIntegrationName(), is(integrationName));
	}

	@Then("^Camilla is presented with \"([^\"]*)\" integration details$")
	public void verifyIntegrationDetails(String integrationName) {
		log.info("Integration detail editPage must show integration name");
		assertThat(detailPage.getIntegrationName(), is(integrationName));
	}

	@When("^Camilla selects the \"([^\"]*)\" integration.*$")
	public void selectConnection(String itegrationName) {
		listPage.getListComponent().goToIntegrationDetail(itegrationName);
	}

	@When("^she selects \"([^\"]*)\" integration action$")
	public void selectIntegrationAction(String action) {
		if ("Create Opportunity".equals(action)) {
			log.warn("Action {} is not available", action);
			editPage.getListActionsComponent().selectAction("Create Salesforce object");
		}
		editPage.getListActionsComponent().selectAction(action);
	}

	@When("^Camilla deletes the \"([^\"]*)\" integration*$")
	public void deleteIntegration(String integrationName) {
		listPage.getListComponent().clickDeleteIntegration(integrationName);
	}

	@When("^Camilla deletes the integration on detail page*$")
	public void deleteIntegrationOnDetailPage() {
		detailPage.deleteIntegration();
	}

	@Then("^she can see on detail editPage that integration is \"([^\"]*)\" status$")
	public void checkStatus(String expectedStatus) {
		String status = detailPage.getStatus();
		log.info("Status: {}", status);
		assertThat(expectedStatus, is(status));
	}

	@Then("^she clicks on integration in \"([^\"]*)\" status and check on detail if status match and appropriate actions are available$")
	public void clickOnIntegrationInStatus(String status) {
		SelenideElement integrationByStatus = listPage.getListComponent().getIntegrationByStatus(status);
		integrationByStatus.shouldBe(visible).click();
		IntegrationDetailPage detailPageSpecific = detailPage.getDetailPage(status);
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
		ElementsCollection integrations = listPage.getListComponent().getAllIntegrations();

		for (SelenideElement integration : integrations) {
			String status = listPage.getListComponent().getIntegrationItemStatus(integration);
			log.info("Status: {}", status);
			integration.shouldBe(visible).click();
			IntegrationDetailPage detailPageSpecific = detailPage.getDetailPage(status);
			for (String action : detailPageSpecific.actionsSet) {
				log.info("Action: {}", action);
				log.info("There should by button for {} action on {} status", action, status);
				detailPageSpecific.getActionButton(action).shouldBe(visible);
			}
			log.info("Status on detail editPage should be equal to expected status:");
			assertThat(detailPageSpecific.getStatus(), is(status));
			detailPageSpecific.done();
		}
	}

	@When("^she selects \"([^\"]*)\" integration step$")
	public void addStep(String stepName) {
		log.info("Adding {} step to integration", stepName);
		editPage.getAddStepComponent().addStep(stepName);
	}

	@Then("^Integration \"([^\"]*)\" is present in integrations list$")
	public void expectIntegrationPresent(String name) {
		log.info("Verifying integration {} is present", name);
		assertThat(listPage.getListComponent().isIntegrationPresent(name), is(true));
	}

	@Then("^Camilla can not see \"([^\"]*)\" integration anymore$")
	public void expectIntegrationNotPresent(String name) {
		log.info("Verifying if integration {} is present", name);
		assertThat(listPage.getListComponent().isIntegrationPresent(name), is(false));
	}

	@Then("^she wait until integration \"([^\"]*)\" get into \"([^\"]*)\" state$")
	public void waitForIntegrationState(String integrationName, String integrationStatus) {
		SelenideElement integration = listPage.getListComponent().getIntegration(integrationName);
		assertTrue(TestUtils.waitForEvent(status -> status.equals(integrationStatus), () -> listPage.getListComponent().getIntegrationItemStatus(integration),
				TimeUnit.MINUTES, 5, TimeUnit.SECONDS, 5));
	}

	@Then("^she is presented with a add step page$")
	public void addStepPageOpened() {
		log.info("there must be add step page root element");
		editPage.getAddStepComponent().getRootElement().shouldBe(visible);
	}

	@Then("^she is presented with a \"([^\"]*)\" step configure page$")
	public void configureStepPageOpen(String stepType) {
		StepComponent stepComponent = editPage.getStepComponent(stepType, "");
		log.info("there must be add step editPage root element");
		stepComponent.getRootElement().shouldBe(visible);
		assertThat(stepComponent.validate(), is(true));
	}

	@Then("^she fill configure page for \"([^\"]*)\" step with \"([^\"]*)\" parameter$")
	public void fillStepConfiguration(String stepType, String parameter) {
		StepComponent stepComponent = editPage.getStepComponent(stepType, parameter);
		stepComponent.fillConfiguration();
	}

	@Then("^she adds \"(\\d+)\" random steps and then check the structure$")
	public void addRandomStepsAndCheckRest(Integer numberOfSteps) {
		log.info("Adding random steps");
		List<String> list = editPage.getFlowViewComponent().getStepsArray();
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
			editPage.getAddStepComponent().addStep(stepType);
			StepComponent stepComponent = editPage.getStepComponent(stepType, stepParameter);
			stepComponent.fillConfiguration();
			editPage.clickButton("Next");
			editPage.clickButton("Add a Step");
			list.add(randomIndex, stepParameter);
		}
		List<String> list2 = editPage.getFlowViewComponent().getStepsArray();
		for (int i = 0; i < list2.size(); i++) {
			log.info("assserting {} and {}", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she delete \"(\\d+)\" random steps and check rest$")
	public void deleteRandomStepsAndCheckRest(Integer numberOfSteps) {
		log.info("Deleting random steps");
		List<String> list = editPage.getFlowViewComponent().getStepsArray();
		ElementsCollection deletes = editPage.getFlowViewComponent().getAllTrashes().shouldBe(sizeGreaterThanOrEqual(1));
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
		List<String> list2 = editPage.getFlowViewComponent().getStepsArray();
		for (int i = 0; i < list.size(); i++) {
			log.info("assserting {} and {", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she delete step on position \"(\\d+)\" and check rest$")
	public void deleteStepOnPositionAndCheckRest(Integer positionOfStep) {
		log.info("Deleting step on position {}", positionOfStep);
		List<String> list = editPage.getFlowViewComponent().getStepsArray();
		ElementsCollection deletes = this.editPage.getFlowViewComponent().getAllTrashes().shouldBe(sizeGreaterThanOrEqual(1));
		Integer indexOfStep = positionOfStep + 1;
		deletes.get(indexOfStep).click();
		editPage.clickOnFirstVisibleButton("OK");
		list.remove(positionOfStep);
		//NOW CHECK:
		List<String> list2 = editPage.getFlowViewComponent().getStepsArray();
		for (int i = 0; i < list.size(); i++) {
			log.info("assserting {} and {}", list.get(i), list2.get(i));
			assertThat(list.get(i), is(list2.get(i)));
		}
	}

	@Then("^she is presented with an actions list$")
	public void expectActionListIsPresent() {
		log.info("There must be action list loaded");
		editPage.getListActionsComponent().getRootElement().shouldBe(visible);
	}

	@Then("^add new basic filter rule with \"([^\"]*)\" parameters$")
	public void addBasicFilterRule(String rule) {
		BasicFilterStepComponent basicFilterStepPage = (BasicFilterStepComponent) editPage.getStepComponent("BASIC FILTER", "");
		basicFilterStepPage.initialize();
		basicFilterStepPage.addRule(rule);
	}

	@Then("^delete \"(\\d+)\" random basic filter rule$")
	public void deleteRandomFilterRules(Integer numberOfRules) {
		for (int i = 0; i < numberOfRules; i++) {
			editPage.getFlowViewComponent().clickRandomTrash();
		}
	}

	@Then("^delete basic filter rule on position \"(\\d+)\"$")
	public void deleteFilterRuleOnPosition(Integer position) {
		ElementsCollection trashes = editPage.getFlowViewComponent().getAllTrashes();
		trashes.get(position - 1).click();
	}

	//Kebab menu test, #553 -> part #548, #549.
	@When("^clicks on the kebab menu icon of each available Integration and checks whether menu is visible and has appropriate actions$")
	public void clickOnAllKebabMenus() {
		listPage.getListComponent().checkAllIntegrationsKebabButtons();
	}

	// Twitter search specification
	@Then("^she fills keywords field with random text to configure search action$")
	public void fillKeywords() {
		String value = Utils.randomString(20);
		editPage.getTwitterSearchComponent().fillInput(value);
	}

	@Then("^she fills \"(\\w+)\" action configure component input with \"([^\"]*)\" value$")
	public void fillActionConfigureField(String fieldId, String value) {
		log.info("Input should be visible");
		editPage.getActionConfigureComponent().fillInput(fieldId, value);
	}

	@Then("^she fills periodic query input with \"([^\"]*)\" value$")
	public void fillPerodicSQLquery(String query) {
		editPage.getPeriodicSqlComponent().fillSqlInput(query);
	}

	@Then("^she fills period input with \"([^\"]*)\" value$")
	public void fillSQLperiod(String period) {
		editPage.getPeriodicSqlComponent().fillSQLperiod(period);
	}

	@Then("^she fills invoke query input with \"([^\"]*)\" value$")
	public void fillInvokeSQLquery(String query) {
		editPage.getInvokeSqlComponent().fillSqlInput(query);
	}

	/**
	 * whether it's start or finish connection
	 *
	 * @param position
	 */
	@Then("^she is prompted to select a \"([^\"]*)\" connection from a list of available connections$")
	public void verifyTypeOfConnection(String position) {
		log.info("{} connection must be active", position);
		assertTrue("There was no active icon found for position " + position, editPage.getFlowViewComponent().verifyActivePosition(position));
	}

	@And("^she is presented with sql-warning$")
	public void checkSqlWarning() throws Throwable {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^she adds first step between START and STEP connection$")
	public void sheAddsFirstStep() throws Throwable {
		editPage.getFlowViewComponent().clickAddStepLink(0);
	}

	@When("^she adds second step between STEP and FINISH connection$")
	public void sheAddsSecond() throws Throwable {
		editPage.getFlowViewComponent().clickAddStepLink(2);
	}

	@And("^sets jms subscribe inputs source data$")
	public void setJmsSubscribeData(DataTable sourceMappingData) {
		for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
			for (String field : source.keySet()) {
				SelenideElement element = editPage.getJmsSubscribeComponent().checkAndGetFieldType(field);
				assertThat(element, notNullValue());
				editPage.getJmsSubscribeComponent().setElementValue(source.get(field), element);
			}
		}
	}

	@And("^sets jms request inputs source data$")
	public void setJmsRequestData(DataTable sourceMappingData) {
		for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
			for (String field : source.keySet()) {
				SelenideElement element = editPage.getJmsSubscribeComponent().checkAndGetFieldType(field);
				assertThat(element, notNullValue());
				editPage.getJmsSubscribeComponent().setElementValue(source.get(field), element);
			}
		}
	}

	@And("^sets jms publish inputs source data$")
	public void setJmsPublishData(DataTable sourceMappingData) {
		for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
			for (String field : source.keySet()) {
				SelenideElement element = editPage.getJmsPublishComponent().checkAndGetFieldType(field);
				assertThat(element, notNullValue());
				editPage.getJmsPublishComponent().setElementValue(source.get(field), element);
			}
		}
	}
}
