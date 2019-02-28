package io.syndesis.qe.rest.tests.integrations.steps;

import java.util.Map;

import cucumber.api.java.en.Given;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class ActiveMQSteps extends AbstractStep {
    @Given("^create ActiveMQ \"([^\"]*)\" action step with destination type \"([^\"]*)\" and destination name \"([^\"]*)\"$")
    public void createAmqStep(String action, String destinationType, String destinationName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.ACTIVEMQ.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.ACTIVEMQ.getId());
        super.addProperty(StepProperty.ACTION, action);
        Map<String, String> properties = TestUtils.map(
                "destinationType", destinationType.toLowerCase().equals("queue") ? "queue" : "topic",
                "destinationName", destinationName
        );
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }
}
