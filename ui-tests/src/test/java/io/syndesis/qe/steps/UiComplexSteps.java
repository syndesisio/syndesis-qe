package io.syndesis.qe.steps;

import org.springframework.beans.factory.annotation.Autowired;

import io.syndesis.qe.bdd.validation.CommonValidationSteps;
import io.syndesis.qe.rest.tests.integrations.DbSteps;
import io.syndesis.qe.rest.tests.integrations.DataMapperStep;
//         io.syndesis.qe.steps.integrations

import java.io.IOException;

import cucumber.api.java.en.Given;
import io.syndesis.qe.rest.tests.integrations.IntegrationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UiComplexSteps {

    @Autowired
    private DbSteps dbSteps;
    @Autowired
    private DataMapperStep mapperSteps;
    @Autowired
    private IntegrationHandler integrationHandler;
    @Autowired
    private CommonValidationSteps commonValidationSteps;

    @Given("^db to db \"([^\"]*)\" integration with period (\\d+) ms$")
    public void dbToDbIntegrationWithPeriodMs(String integrationName, int ms) throws IOException {

        final String mapperFrom = "first_name";
        final String mapperTo = "TASK";
        final String sqlStartQuery = "SELECT * FROM CONTACT";
        final String sqlFinishQuery = "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)";

//1.        @Then("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms")
        dbSteps.createStartDbPeriodicSqlStep(sqlStartQuery, ms);

//2.A @And("start mapper definition with name: \"([^\"]*)\"")
        mapperSteps.startMapperDefinition("mapping1");

//2.B @Then("MAP using Step (\\d+) and field \"([^\"]*)\" to \"([^\"]*)\"")
        mapperSteps.mapDataMapperStep(1, mapperFrom, mapperTo);

//3.        @Then("^create finish DB invoke sql action step with query \"([^\"]*)\"")
        dbSteps.createFinishDbInvokeSqlStep(sqlFinishQuery);

//4.    @When("^create integration with name: \"([^\"]*)\"")
        integrationHandler.createActiveIntegrationFromGivenSteps(integrationName);

//5.        @Then("^wait for integration with name: \"([^\"]*)\" to become active")
        commonValidationSteps.waitForIntegrationToBeActive(integrationName);
    }
}
