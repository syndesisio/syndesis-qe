package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.Given;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class ActiveMQSteps extends AbstractStep {
    @Given("^create ActiveMQ \"([^\"]*)\" action step with destination type \"([^\"]*)\" and destination name \"([^\"]*)\"$")
    public void createAmqStep(String action, String destinationType, String destinationName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.ACTIVEMQ.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.ACTIVEMQ.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
                "destinationType", destinationType.toLowerCase().equals("queue") ? "queue" : "topic",
                "destinationName", destinationName
        ));
        super.createStep();
    }
}
