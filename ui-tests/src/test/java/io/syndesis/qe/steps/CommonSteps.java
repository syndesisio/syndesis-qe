package io.syndesis.qe.steps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.SyndesisPage;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.dashboard.DashboardPage;
import io.syndesis.qe.pages.integrations.list.IntegrationsListComponent;
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
	public void clickOnButton(String buttonTitle) {
		new SyndesisRootPage().clickButton(buttonTitle);
	}

	@When("clicks? on the \"([^\"]*)\" link.*$/")
	public void clickOnLink(String linkTitle) {
		new SyndesisRootPage().clickLink(linkTitle);
	}

	@When("clicks? on the random \"([^\"]*)\" link.*$/")
	public void clickOnLinkRandom(String linkTitle) {
		new SyndesisRootPage().clickLinkRandom(linkTitle);
	}

	@Then("^she is presented with the \"([^\"]*)\" button.*$/")
	public void checkButtonIsVisible(String buttonTitle) {
		new SyndesisRootPage().getButton(buttonTitle).shouldBe(visible);
	}

	@Then("^she is presented with the \"([^\"]*)\" tables*$/")
	public void checkTableTitlesArePresent(String tableTitles) {

		String[] titles = tableTitles.split(",");

		for(String title: titles) {
			new SyndesisRootPage().getTitleByText(title).shouldBe(visible);
		}
	}

	public void expectTableTitlePresent(String tableTitle) {
		SelenideElement table = new SyndesisRootPage().getTitleByText(tableTitle);
		table.shouldBe(visible);
	}

	@Then("^she is presented with the \"([^\"]*)\" elements*$/")
	public void expectElementsPresent(String elementClassNames) {
		String[] elementClassNamesArray = elementClassNames.split(",");

		for (String elementClassName : elementClassNamesArray) {
			SelenideElement element = new SyndesisRootPage().getElementByClassName(elementClassName);
			element.shouldBe(visible);
		}
	}

	@Then("^she is presented with the \"([^\"]*)\"$/")
	public void expectElementPresent(String elementClassName) {
		SelenideElement element = new SyndesisRootPage().getElementByClassName(elementClassName);
		element.shouldBe(visible);
	}

	@Then("^Integration \"([^\"]*)\" is present in top 5 integrations$/")
	public void expectIntegrationPresentinTopFive(String name) {
		log.info("Verifying integration {} is present in top 5 integrations", name);
		DashboardPage dashboardPage = new DashboardPage();
		Assertions.assertThat(dashboardPage.isIntegrationPresent(name));
	}

	@Then("^Camilla can see \"([^\"]*)\" connection on dashboard page$/")
	public void expectConnectionTitlePresent (String connectionName) {
		DashboardPage dashboardPage = new DashboardPage();
		SelenideElement connection = dashboardPage.getConnection(connectionName);
		connection.shouldBe(visible);
	}

	@Then("^Camilla can not see \"([^\"]*)\" connection on dashboard page anymore$/")
	public void expectConnectionTitleNonPresent (String connectionName) {
		DashboardPage dashboardPage = new DashboardPage();
		SelenideElement connection = dashboardPage.getConnection(connectionName);
		connection.shouldNotBe(visible);
	}

	@When("^Camilla deletes the \"([^\"]*)\" integration in top 5 integrations$/")
	public void deleteIntegrationOnDashboard(String integrationName) {
		log.info("Trying to delete {} on top 5 integrations table");
		IntegrationsListComponent listComponent = new IntegrationsListComponent();
		listComponent.clickDeleteIntegration(integrationName);
	}

	@Then("^Camilla can not see \"([^\"]*)\" integration in top 5 integrations anymore$/")
	public void expectIntegrationNotPresentOnDashboard(String name) {
		log.info("Verifying if integration {} is present", name);
		DashboardPage dashboardPage = new DashboardPage();
		Assertions.assertThat(dashboardPage.isIntegrationPresent(name)).isFalse();
	}

	@Then("^she can see success notification$/")
	public void successNotificationIsPresent() {
		SelenideElement allertSucces = new SyndesisRootPage().getElementByClassName("alert-success");
		allertSucces.shouldBe(visible);
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
		WebDriver driver = WebDriverRunner.getWebDriver();
		JavascriptExecutor jse = (JavascriptExecutor) driver;

		int x = 0;
		int y = 0;

		int width = (int) jse.executeScript("return $(document).width()");
		int height = (int) jse.executeScript("return $(document).height()");
	
		if (leftRight.equals("right")) {
			y = width;
		}

		if (topBottom.equals("bottom")) {
			x = height;
		}
	
		jse.executeScript("(browserX, browserY) => window.scrollTo(browserX, browserY)", x, y);
	}

	@Then("^(\\w+)? is presented with the Syndesis page \"([^\"]*)\"$/")
	public void validatePage(String pageName) {
		SyndesisPage.get(pageName).validate();
	}
}
