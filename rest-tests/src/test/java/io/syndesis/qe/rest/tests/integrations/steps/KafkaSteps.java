package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class KafkaSteps extends AbstractStep {
    @When("^create Kafka \"([^\"]*)\" step with topic \"([^\"]*)\"$")
    public void createKafkaStep(String action, String topic) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.KAFKA.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.KAFKA.getId());
        super.addProperty(StepProperty.ACTION, "kafka-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("topic", topic));
        super.createStep();
    }
}
