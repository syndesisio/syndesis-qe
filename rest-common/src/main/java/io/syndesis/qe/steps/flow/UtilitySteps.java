package io.syndesis.qe.steps.flow;

import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.integration.Step;
import io.syndesis.qe.entities.StepDefinition;
import io.syndesis.qe.storage.StepsStorage;

import io.cucumber.java.en.Given;

public class UtilitySteps extends AbstractStep {
    @Given("change {string} datashape of previous step to {string} type with specification {string}")
    public void changeDatashapeTo(String direction, String type, String specification) {
        Step lastStep = super.getSteps().getLastStepDefinition().getStep();
        Step withDatashape = new Step.Builder().createFrom(lastStep).action(
                super.withCustomDatashape(
                        lastStep.getAction().get(),
                        super.getConnectorDescriptor(
                                lastStep.getAction().get(), lastStep.getConfiguredProperties(), lastStep.getConnection().get().getId().get()
                        ),
                        direction,
                        DataShapeKinds.valueOf(type),
                        specification
                )
        ).build();

        StepsStorage steps = super.getSteps();
        steps.getStepDefinitions().remove(steps.getStepDefinitions().size() - 1);
        steps.getStepDefinitions().add(new StepDefinition(withDatashape));
    }
}
