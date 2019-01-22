package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
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
    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    @Then("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicSqlStep(String sqlQuery, Integer ms) {
        final Connector dbConnector = connectorsEndpoint.get(RestTestsUtils.Connection.DB.getId());
        final Connection dbConnection = connectionsEndpoint.get(RestTestsUtils.Connector.DB.getId());
        final Action dbAction = TestUtils.findConnectorAction(dbConnector, "sql-start-connector");
        final Map<String, String> properties = TestUtils.map("query", sqlQuery, "schedulerExpression", ms);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dbAction, properties, dbConnection.getId().get());

        //to be reported: period is not part of .json step (when checked via browser).
        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(generateStepAction(dbAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep, connectorDescriptor));
    }

    @Then("^create start DB periodic stored procedure invocation action step named \"([^\"]*)\" and period \"([^\"]*)\" ms")
    public void createStartDbPeriodicProcedureStep(String procedureName, Integer ms) {
        final Connector dbConnector = connectorsEndpoint.get(RestTestsUtils.Connection.DB.getId());
        final Connection dbConnection = connectionsEndpoint.get(RestTestsUtils.Connector.DB.getId());
        final Action dbAction = TestUtils.findConnectorAction(dbConnector, "sql-stored-start-connector");
        final Map<String, String> properties = TestUtils.map("procedureName", procedureName, "schedulerExpression", ms,
                "template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})");
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dbAction, properties, dbConnection.getId().get());

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(generateStepAction(dbAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep, connectorDescriptor));
    }

    @Then("^create finish DB invoke sql action step with query \"([^\"]*)\"")
    public void createFinishDbInvokeSqlStep(String sqlQuery) {
        final Connector dbConnector = connectorsEndpoint.get(RestTestsUtils.Connection.DB.getId());
        final Connection dbConnection = connectionsEndpoint.get(RestTestsUtils.Connector.DB.getId());
        final Action dbAction = TestUtils.findConnectorAction(dbConnector, "sql-connector");
        final Map<String, String> properties = TestUtils.map("query", sqlQuery);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dbAction, properties, dbConnection.getId().get());

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(generateStepAction(dbAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep, connectorDescriptor));
    }

    @Given("^create DB step with query: \"([^\"]*)\" and interval: (\\d+) miliseconds")
    public void createDbStepWithInterval(String query, int interval) {
        final Connector dbConnector = connectorsEndpoint.get(RestTestsUtils.Connection.DB.getId());
        final Connection dbConnection = connectionsEndpoint.get(RestTestsUtils.Connector.DB.getId());
        final Map<String, String> properties = TestUtils.map("query", query, "schedulerExpression", interval);
        final Action dbAction = TestUtils.findConnectorAction(dbConnector, "sql-connector");
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dbAction, properties, dbConnection.getId().get());

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dbConnection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(dbAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep, connectorDescriptor));
    }

    @And("^create finish DB invoke stored procedure \"([^\"]*)\" action step")
    public void createFinishDbInvokeProcedureStep(String procedureName) {
        final Connector dbConnector = connectorsEndpoint.get(RestTestsUtils.Connection.DB.getId());
        final Connection dbConnection = connectionsEndpoint.get(RestTestsUtils.Connector.DB.getId());
        final Action dbAction = TestUtils.findConnectorAction(dbConnector, "sql-stored-connector");
        final Map<String, String> properties = TestUtils.map("procedureName", procedureName);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dbAction, properties, dbConnection.getId().get());

        properties.put("template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
                + "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})");

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(generateStepAction(TestUtils.findConnectorAction(dbConnector, "sql-stored-connector"), connectorDescriptor))
                .configuredProperties(properties)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep, connectorDescriptor));
    }
}
