package proofofconcept.steps;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import proofofconcept.pages.OpenshiftLogin;

public class LoginSteps {


	@Given("^Camilla logs in on Openshift login page$")
	public void camilla_logs_in_on_openshift_login_page() throws Throwable {
		OpenshiftLogin openshiftLoginPage = new OpenshiftLogin();

		Selenide.open("https://syndesis.192.168.42.241.nip.io");
		openshiftLoginPage.logIn("a","a");
	}

	@When("^Camilla fills in her username and password$")
	public void camilla_fills_in_her_username_and_password() {
		System.out.println("CUCUCUCUCUCUCUMBERRRRRRRRRRR PROOOOOOOOOOOF OF CONCEPTTTTTTTT");
	}

	@And("^Logs in Openshift$")
	public void logs_in_openshift() {
		System.out.println("CUCUCUCUCUCUCUMBERRRRRRRRRRR PROOOOOOOOOOOF OF CONCEPTTTTTTTT");
	}

	@When("^Camilla updates account information")
	public void camilla_updates_account_information() {
		System.out.println("CUCUCUCUCUCUCUMBERRRRRRRRRRR PROOOOOOOOOOOF OF CONCEPTTTTTTTT");
	}

	@Then("^Camilla can see syndesis home page")
	public void camilla_can_see_syndesis_home_page() {
		System.out.println("CUCUCUCUCUCUCUMBERRRRRRRRRRR PROOOOOOOOOOOF OF CONCEPTTTTTTTT");
	}
}
