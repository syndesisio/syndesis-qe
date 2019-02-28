package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import cucumber.api.java.en.When;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.StepDescriptorEndpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SplitSteps extends AbstractStep {
    @Autowired
    private StepsStorage steps;
    @Autowired
    private StepDescriptorEndpoint stepDescriptorEndpoint;

    @When("^add a split step$")
    public void addSplitStep() {
        final DataShape in = steps.getLastStepDefinition().getStep().getAction().get().getInputDataShape().get();
        final DataShape out = steps.getLastStepDefinition().getStep().getAction().get().getOutputDataShape().get();
        StepDescriptor sd = stepDescriptorEndpoint.postParamsAction(
                "split",
                new DynamicActionMetadata.Builder().inputShape(in).outputShape(out).build()
        );
        final Step split = new Step.Builder()
                .stepKind(StepKind.split)
                .id(UUID.randomUUID().toString())
                .name("Split")
                .action(generateStepAction(steps.getLastStepDefinition().getStep().getAction().get(), sd))
                .build();
        steps.getStepDefinitions().add(new StepDefinition(split));
    }
}
