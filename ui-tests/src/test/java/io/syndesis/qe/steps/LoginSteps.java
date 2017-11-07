package io.syndesis.qe.steps;

import com.codeborne.selenide.Selenide;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.configuration.TestConfiguration;
import io.syndesis.qe.pages.Home;
import io.syndesis.qe.pages.OpenshiftLogin;

public class LoginSteps {

	@Given("^\"(\\w+)\" lCogs into the Syndesis.*$/i")
	public void login() throws Throwable {
		OpenshiftLogin openshiftLoginPage = new OpenshiftLogin();

		Selenide.open(TestConfiguration.getSyndesisLoginUrl());
		openshiftLoginPage.logIn(TestConfiguration.getSyndesisLoginUsername(), TestConfiguration.getSyndesisLoginPassword());
	}

	@Then("^\"(\\w+)\" is presented with the Syndesis home page.")
	public void checkHomePageVisibility() {
		Home home = new Home();
		home.checkVisibility(true);
	}
}
