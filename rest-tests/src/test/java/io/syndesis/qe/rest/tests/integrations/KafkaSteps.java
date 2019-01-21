package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.syndesis.common.model.DataShapeKinds;
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

public class KafkaSteps extends AbstractStep {
    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    private Connection kafkaConnection;
    private Action kafkaAction;
    private Map<String, String> properties;

    private void init(String action, String topic) {
        final Connector kafkaConnector = connectorsEndpoint.get(RestTestsUtils.Connector.KAFKA.getId());
        kafkaConnection = connectionsEndpoint.get(RestTestsUtils.Connection.KAFKA.getId());
        kafkaAction = TestUtils.findConnectorAction(kafkaConnector, action);
        properties = TestUtils.map(
                "topic", topic
        );
    }

    @When("^create Kafka publish step with datashape and with topic \"([^\"]*)\"$")
    public void createKafkaPublishStepWithDatashape(String topic) {
        init("kafka-publish-action", topic);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(kafkaAction, properties, RestTestsUtils.Connection.KAFKA.getId());

        final Step kafkaStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(kafkaConnection)
                .action(withCustomDatashape(kafkaAction, connectorDescriptor, "in", DataShapeKinds.JSON_SCHEMA, "{\"$schema\":\"http://json" +
                        "-schema.org/draft-04/schema#\",\"type\":\"object\",\"properties\":{\"Id\":{\"type\":\"string\"}},\"required\":[\"Id\"]}"))
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(kafkaStep));
    }

    @Given("^create Kafka subscribe step with topic \"([^\"]*)\"$")
    public void createKafkaSubscribeStepWithTopic(String topic) {
        init("kafka-subscribe-action", topic);

        final Step kafkaStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(kafkaConnection)
                .action(kafkaAction)
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(kafkaStep));
    }
}
