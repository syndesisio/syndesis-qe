package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;

public class AmqSteps {
    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    public AmqSteps() {
    }

    @Given("^create AMQ \"([^\"]*)\" action step with destination type \"([^\"]*)\" and destination name \"([^\"]*)\"$")
    public void createAmqStep(String action, String destinationType, String destinationName) {
        final Connector amqConnector = connectorsEndpoint.get("activemq");
        final Connection amqConnection = connectionsEndpoint.get(RestConstants.AMQ_CONNECTION_ID);
        final Action connectorAction = TestUtils.findConnectorAction(amqConnector, action);
        final Map<String, String> properties = TestUtils.map(
                "destinationType", destinationType.toLowerCase().equals("queue") ? "queue" : "topic",
                "destinationName", destinationName
        );

        final Step amqStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(amqConnection)
                .action(connectorAction)
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(amqStep));
    }
}
