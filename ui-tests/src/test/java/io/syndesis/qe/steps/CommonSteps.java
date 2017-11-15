package io.syndesis.qe.steps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.login.GitHubLoginPage;
import io.syndesis.qe.pages.login.MinishiftLoginPage;
import io.syndesis.qe.pages.login.RHDevLoginPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {

	@Given("^\"([^\"]*)\" logs into the Syndesis\\.$")
	public void login(String username) throws Throwable {
		Selenide.open(TestConfiguration.syndesisUrl());

		String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();

		if (currentUrl.contains("api.fuse-ignite.openshift.com")) {
			log.info("Ignite cluster login page");
			$(By.className("login-redhat")).shouldBe(visible).click();

			RHDevLoginPage rhDevLoginPage = new RHDevLoginPage();
			rhDevLoginPage.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
		} else if (currentUrl.contains(":8443/login")) {
			log.info("Minishift cluster login page");

			MinishiftLoginPage minishiftLoginPage = new MinishiftLoginPage();
			minishiftLoginPage.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
		} else if (currentUrl.contains("github.com/login")) {

			GitHubLoginPage gitHubLoginPage = new GitHubLoginPage();
			gitHubLoginPage.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
		} else {
			log.error("No suitable login method found");
		}

		currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();

		if (currentUrl.contains("oauth/authorize/approve")) {
			log.info("Authorize access login page");
			$("input[name=\"approve\"]").shouldBe(visible).click();
		}
	}

	@Given("^clean application state$")
	public void resetState() {
		Long result = (Long) ((JavascriptExecutor) WebDriverRunner.getWebDriver())
				.executeAsyncScript("var callback = arguments[arguments.length - 1]; " +
						"$.get('/api/v1/test-support/reset-db', function(data, textStatus, jqXHR) { callback(jqXHR.status); })");

		Assertions.assertThat(String.valueOf(result)).isEqualTo("204");
	}

	@Then("^\"(\\w+)\" is presented with the Syndesis home page.")
	public void checkHomePageVisibility(String username) {
		SyndesisRootPage syndesisRootPage = new SyndesisRootPage();
		syndesisRootPage.getRootElement().shouldBe(visible);
	}

	@Then("^(\\w+)? is presented with the \"([^\"]*)\" link*$/")
	public void validateLink(String alias, String linkTitle) {
		new SyndesisRootPage().getLink(linkTitle).shouldBe(visible);
	}

	@When("clicks? on the \"([^\"]*)\" button.*$/")
	public void clickOnButton(String buttonTitle, String callback) {
		new SyndesisRootPage().clickButton(buttonTitle);
	}

	@When("clicks? on the \"([^\"]*)\" link.*$/")
	public void clickOnLink(String linkTitle, String callback) {
		new SyndesisRootPage().clickLink(linkTitle);
	}

	@When("clicks? on the random \"([^\"]*)\" link.*$/")
	public void clickOnLinkRandom(String linkTitle, String callback) {
		new SyndesisRootPage().clickLinkRandom(linkTitle);
	}

	@Then("^she is presented with the \"([^\"]*)\" button.*$/")
	public void checkButtonIsVisible(String buttonTitle, String callback) {
		new SyndesisRootPage().getButton(buttonTitle).shouldBe(visible);
	}

	@Then("^she is presented with the \"([^\"]*)\" tables*$/")
	public void checkTableTitlesArePresent(String tableTitles, String callback) {

		throw new UnsupportedOperationException();
//
//		String[] titles = tableTitles.split(",");
//
//		for(String title: titles) {
//			new SyndesisRootPage().getElementContainingText()
//		}
//		.getElementContainingText()
//		for (const tableTitle of tableTitlesArray) {
//			this.expectTableTitlePresent(tableTitle, callback);
//		}
	}

	public void expectTableTitlePresent(String tableTitle, String callback) {

		throw new UnsupportedOperationException();
//    const table = this.world.app.getTitleByText(tableTitle);
//		expect(table.isPresent(), `There must be present a table ${tableTitle}`)
//      .to.eventually.be.true;
//
//		expect(table.isPresent(), `There must be enabled table ${tableTitle}`)
//      .to.eventually.be.true.notify(callback);
	}

	@Then("^she is presented with the \"([^\"]*)\" elements*$/")
	public void expectElementsPresent(String elementClassNames, String callback) {

		throw new UnsupportedOperationException();
//    const elementClassNamesArray = elementClassNames.split(',');
//
//		for (const elementClassName of elementClassNamesArray) {
//			this.expectElementPresent(elementClassName, callback);
//		}
	}

	@Then("^she is presented with the \"([^\"]*)\"$/")
	public void expectElementPresent(String elementClassName, String callback) {

		throw new UnsupportedOperationException();
//    const element = this.world.app.getElementByClassName(elementClassName);
//		expect(element.isPresent(), `There must be present a element ${elementClassName}`)
//      .to.eventually.be.true;
//
//		expect(element.isPresent(), `There must be enabled element ${elementClassName}`)
//      .to.eventually.be.true.notify(callback);
	}

	@Then("^Integration \"([^\"]*)\" is present in top 5 integrations$/")
	public void expectIntegrationPresentinTopFive(String name, String callback) {

		throw new UnsupportedOperationException();
//		log.info(`Verifying integration ${name} is present in top 5 integrations`);
//    const page = new DashboardPage();
//		expect(page.isIntegrationPresent(name), `Integration ${name} must be present`)
//      .to.eventually.be.true.notify(callback);
	}

	@Then("^Camilla can see \"([^\"]*)\" connection on dashboard page$/")
	public void expectConnectionTitlePresent (String connectionName, String callback) {

		throw new UnsupportedOperationException();
//    const dashboard = new DashboardPage();
//    const connection = dashboard.getConnection(connectionName);
//		expect(connection.isPresent(), `There should be present connection ${connectionName}`)
//      .to.eventually.be.true.notify(callback);
	}

	@Then("^Camilla can not see \"([^\"]*)\" connection on dashboard page anymore$/")
	public void expectConnectionTitleNonPresent (String connectionName, String callback) {

		throw new UnsupportedOperationException();
//    const dashboard = new DashboardPage();
//    const connection = dashboard.getConnection(connectionName);
//		expect(connection.isPresent(), `There shouldnt be a present connection ${connectionName}`)
//      .to.eventually.be.false.notify(callback);
	}

	@When("^Camilla deletes the \"([^\"]*)\" integration in top 5 integrations$/")
	public void deleteIntegrationOnDashboard(String integrationName) {

		throw new UnsupportedOperationException();
//		log.info(`Trying to delete ${integrationName} on top 5 integrations table`);
//    const listComponent = new IntegrationsListComponent();
//		return listComponent.clickDeleteIntegration(integrationName);
	}

	@Then("^Camilla can not see \"([^\"]*)\" integration in top 5 integrations anymore$/")
	public void expectIntegrationPresentOnDashboard(String name, String callback) {

		throw new UnsupportedOperationException();
//		log.info(`Verifying if integration ${name} is present`);
//    const dashboard = new DashboardPage();
//		expect(dashboard.isIntegrationPresent(name), `Integration ${name} must be present`)
//      .to.eventually.be.false.notify(callback);
	}

	@Then("^she can see success notification$/")
	public void successNotificationIsPresent() {

		throw new UnsupportedOperationException();
//    const allertSucces = this.world.app.getElementByClassName('alert-success');
//		return browser.wait(ExpectedConditions.visibilityOf(allertSucces), 6000, 'OK button not loaded in time');
	}

	/**
	 * Scroll the webpage.
	 *
	 * @param topBottom possible values: top, bottom
	 * @param leftRight possible values: left, right
	 * @returns {Promise<any>}
	 */
	@When("^scroll \"([^\"]*)\" \"([^\"]*)\"$/")
	public void scrollTo(String topBottom, String leftRight) {

		throw new UnsupportedOperationException();
//		// get real width and height
//    const width = await browser.executeScript(() => $(document).width());
//    const height = await browser.executeScript(() => $(document).height());
//
//    const directions: Object = {
//				top: 0,
//				bottom: height,
//				left: 0,
//				right: width,
//    };
//
//		if (!directions.hasOwnProperty(topBottom) || !directions.hasOwnProperty(leftRight)) {
//			return P.reject(`unknown directions [${topBottom}, ${leftRight}`);
//		}
//    const x = directions[leftRight];
//    const y = directions[topBottom];
//
//		log.info(`scrolling to [x=${x},y=${y}]`);
//		return browser.driver.executeScript((browserX, browserY) => window.scrollTo(browserX, browserY), x, y);
//	}
	}
}
