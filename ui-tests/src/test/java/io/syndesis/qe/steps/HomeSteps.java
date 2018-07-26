package io.syndesis.qe.steps;

import cucumber.api.java.en.Then;
import io.syndesis.qe.steps.integrations.IntegrationSteps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HomeSteps {

    private IntegrationSteps integrationSteps = new IntegrationSteps();

    @Then("^check starting integration ([^\"]*) status on Home page$")
    public void checkStartingStatusOnIntegrationsPage( String integrationName) {
        integrationSteps.checkStartingStatus(integrationName, "Home");
    }

}
