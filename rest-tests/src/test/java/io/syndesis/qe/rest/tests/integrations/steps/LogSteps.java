package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.utils.TestUtils;

public class LogSteps extends AbstractStep {
    @When("^add log step$")
    public void addLogStep() {
        super.addProperty(StepProperty.KIND, StepKind.log);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("contextLoggingEnabled", "true",
                "bodyLoggingEnabled", "true"
        ));
        super.createStep();
    }
}
