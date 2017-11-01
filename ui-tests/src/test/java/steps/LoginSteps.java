package steps;

import com.codeborne.selenide.Selenide;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import pages.Home;
import pages.OpenshiftLogin;
import properties.PropertiesLoader;

public class LoginSteps {

	@Given("^Camilla logs in on Openshift login page$")
	public void camilla_logs_in_on_openshift_login_page() throws Throwable {
		OpenshiftLogin openshiftLoginPage = new OpenshiftLogin();

		Selenide.open(PropertiesLoader.get("login.url"));
		openshiftLoginPage.logIn(PropertiesLoader.get("login.username"), PropertiesLoader.get("login.password"));
	}

	@Then("^Camilla can see syndesis home page")
	public void camilla_can_see_syndesis_home_page() {
		Home home = new Home();
		home.checkVisibility(true);
	}
}
