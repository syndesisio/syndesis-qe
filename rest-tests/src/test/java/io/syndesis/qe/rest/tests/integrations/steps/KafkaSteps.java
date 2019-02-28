package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class KafkaSteps extends AbstractStep {
    @When("^create Kafka \"([^\"]*)\" step with topic \"([^\"]*)\"$")
    public void createKafkaPublishStepWithDatashape(String action, String topic) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DROPBOX.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DROPBOX.getId());
        super.addProperty(StepProperty.ACTION, "kafka-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("topic", topic));
        super.createStep();
    }
}
