package io.syndesis.qe.steps;

import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
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
			$(By.className("login-redhat")).shouldBe(Condition.visible).click();

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
			$("input[name=\"approve\"]").shouldBe(Condition.visible).click();
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
		syndesisRootPage.getRootElement().shouldBe(Condition.visible);
	}
}
