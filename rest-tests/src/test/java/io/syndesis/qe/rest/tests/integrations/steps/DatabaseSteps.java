package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * DB steps for integrations.
 * <p>
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DatabaseSteps extends AbstractStep {
    @Then("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicSqlStep(String sqlQuery, Integer ms) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-start-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("query", sqlQuery, "schedulerExpression", ms));
        super.createStep();
    }

    @Then("^create start DB periodic stored procedure invocation action step named \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicProcedureStep(String procedureName, Integer ms) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-stored-start-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
                "procedureName", procedureName,
                "schedulerExpression", ms,
                "template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                        + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"));
        super.createStep();
    }

    @Then("^create finish DB invoke sql action step with query \"([^\"]*)\"")
    public void createFinishDbInvokeSqlStep(String sqlQuery) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("query", sqlQuery));
        super.createStep();
    }

    @When("^create finish DB invoke stored procedure \"([^\"]*)\" action step")
    public void createFinishDbInvokeProcedureStep(String procedureName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-stored-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
                "procedureName", procedureName,
                "template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                        + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"));
        super.createStep();
    }
}
