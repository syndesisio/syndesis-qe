package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
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
public class DbSteps {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    public DbSteps() {
    }

    //done.
    @Then("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicSqlStep(String sqlQuery, Integer ms) {
        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");

        //to be reported: period is not part of .json step (when checked via browser).
        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .action(TestUtils.findConnectorAction(dbConnector, "sql-start-connector"))
                .configuredProperties(TestUtils.map("query", sqlQuery, "schedulerPeriod", ms))
                .build();
        steps.getSteps().add(dbStep);
    }

    //done. (not used for the time being.)
    @Then("^create start DB periodic stored procedure invocation action step named \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicProcedureStep(String procedureName, Integer ms) {
        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");
        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .action(TestUtils.findConnectorAction(dbConnector, "sql-stored-start-connector"))
                .configuredProperties(TestUtils.map("procedureName", procedureName, "schedulerPeriod", ms,
                        "template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                        + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"))
                .build();
        steps.getSteps().add(dbStep);
    }

    //done
    @Then("^create finish DB invoke sql action step with query \"([^\"]*)\"")
    public void createFinishDbInvokeSqlStep(String sqlQuery) {
        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .action(TestUtils.findConnectorAction(dbConnector, "sql-connector"))
                .configuredProperties(TestUtils.map("query", sqlQuery))
                .build();
        steps.getSteps().add(dbStep);
    }

    @Given("^create DB step with query: \"([^\"]*)\" and interval: (\\d+) miliseconds")
    public void createDbStepWithInterval(String query, int interval) {
        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");
        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .action(TestUtils.findConnectorAction(dbConnector, "sql-connector"))
                .configuredProperties(TestUtils.map("query", query, "schedulerPeriod", interval))
                .build();
        steps.getSteps().add(dbStep);
    }

    @Then("^create finish DB invoke stored procedure \"([^\"]*)\" action step")
    public void createFinishDbInvokeProcedureStep(String procedureName) {
        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");
        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .action(TestUtils.findConnectorAction(dbConnector, "sql-stored-connector"))
                .configuredProperties(TestUtils.map("procedureName", procedureName,
                        "template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                        + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"))
                .build();
        steps.getSteps().add(dbStep);
    }

//    AUXILIARIES:
    private String getDbConnectionId() {

        final String postgresDbName = "PostgresDB";
        List<Connection> connects = null;

        connects = connectionsEndpoint.list();
        String dbConnectionId = null;
        for (Connection s : connects) {
            if (s.getName().equals(postgresDbName)) {
                dbConnectionId = (String) s.getId().get();
            }
        }
        log.debug("db connection id: " + dbConnectionId);
        return dbConnectionId;
    }
}
