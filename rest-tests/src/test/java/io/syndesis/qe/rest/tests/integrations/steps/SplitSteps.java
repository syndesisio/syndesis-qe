package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.When;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.endpoints.StepDescriptorEndpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SplitSteps extends AbstractStep {
   @Autowired
    private StepDescriptorEndpoint stepDescriptorEndpoint;

    @When("^add a split step$")
    public void addSplitStep() {
        final DataShape in = super.getSteps().getLastStepDefinition().getStep().getAction().get().getInputDataShape().get();
        final DataShape out = super.getSteps().getLastStepDefinition().getStep().getAction().get().getOutputDataShape().get();
        StepDescriptor sd = stepDescriptorEndpoint.postParamsAction(
                "split",
                new DynamicActionMetadata.Builder().inputShape(in).outputShape(out).build()
        );

        super.addProperty(StepProperty.KIND, StepKind.split);
        super.addProperty(StepProperty.ACTION, generateStepAction(super.getSteps().getLastStepDefinition().getStep().getAction().get(), sd));
        super.addProperty(StepProperty.STEP_NAME, "Split");
        super.createStep();
    }
}
