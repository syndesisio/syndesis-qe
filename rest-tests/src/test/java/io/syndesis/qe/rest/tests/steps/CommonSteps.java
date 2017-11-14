package io.syndesis.qe.rest.tests.steps;

import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.templates.SyndesisTemplate;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {

	@Given("user cleans default namespace")
	public void cleanNamespace() {
		OpenShiftUtils.getInstance().cleanProject();
	}

	@When("user deploys Syndesis from template")
	public void deploySyndesis() {
		SyndesisTemplate.deploy();
	}

	@Then("user waits for Syndesis to become ready")
	public void waitForSyndeisis() {
		try {
			OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("component", "syndesis-rest"));
		} catch (InterruptedException | TimeoutException e) {
			log.error("Wait for syndesis-rest failed ", e);
		}
	}
}
