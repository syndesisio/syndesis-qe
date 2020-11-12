package io.syndesis.qe.steps.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.filter.FilterPredicate;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.endpoint.ExtensionsEndpoint;
import io.syndesis.qe.endpoint.StepDescriptorEndpoint;
import io.syndesis.qe.utils.FilterRulesBuilder;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

public class IntermediateSteps extends AbstractStep {
    @Autowired
    private StepDescriptorEndpoint stepDescriptorEndpoint;

    @Autowired
    private ExtensionsEndpoint extensionsEndpoint;

    @When("add advanced filter step with {string} expression")
    public void addAdvancedFilterStepWithExpression(String expression) {
        super.addProperty(StepProperty.KIND, StepKind.expressionFilter);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("filter", expression));
        super.addProperty(StepProperty.STEP_NAME, "Advanced filter");
        super.createStep();
    }

    @When("create basic filter step for {string} with word {string} and operation {string}")
    public void createBasicFilterStepWord(String path, String value, String operation) {
        super.addProperty(StepProperty.KIND, StepKind.ruleFilter);
        super.addProperty(StepProperty.STEP_NAME, "Rule Filter " + path);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
                "type", "rule",
                "predicate", FilterPredicate.AND.toString(),
                "rules", new FilterRulesBuilder().addPath(path).addValue(value).addOps(operation).build()
        ));
        super.createStep();
    }

    @When("add {string} extension step with {string} action")
    public void addExtensionIdWith(String name, String actionId) {
        addExtensionIdWith(name, actionId, null);
    }

    @When("add {string} extension step with {string} action with properties:")
    public void addExtensionIdWith(String name, String actionId, DataTable properties) {
        Optional<Extension> e = extensionsEndpoint.list().stream().filter(ex -> ex.getName().equalsIgnoreCase(name)).findFirst();
        assertThat(e).isPresent();

        final Optional<Action> action = e.get().getActions().stream().filter(act -> act.getId().get().equals(actionId)).findFirst();
        assertThat(action).isPresent();
        super.addProperty(StepProperty.KIND, StepKind.extension);
        super.addProperty(StepProperty.ACTION, action.get());
        if (properties != null) {
            super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        }
        super.addProperty(StepProperty.STEP_NAME, name);
        super.addProperty(StepProperty.EXTENSION, e.get());
        super.createStep();
    }

    @When("add log step")
    public void addLogStep() {
        super.addProperty(StepProperty.KIND, StepKind.log);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("contextLoggingEnabled", "true",
                "bodyLoggingEnabled", "true"
        ));
        super.createStep();
    }

    @When("add a split step")
    public void addSplitStep() {
        final DataShape in = super.getSteps().getLastStepDefinition().getStep().getAction().get().getInputDataShape().get();
        final DataShape out = super.getSteps().getLastStepDefinition().getStep().getAction().get().getOutputDataShape().get();
        StepDescriptor sd = stepDescriptorEndpoint.getStepDescriptor("split", in, out);

        super.addProperty(StepProperty.KIND, StepKind.split);
        super.addProperty(StepProperty.ACTION, super.generateStepAction(super.getSteps().getLastStepDefinition().getStep().getAction().get(), sd));
        super.addProperty(StepProperty.STEP_NAME, "Split");
        super.createStep();
    }

    @When("add an aggregate step")
    public void addAggregateStep() {
        Step previousStepWithDatashapes = null;
        for (int i = super.getSteps().getSteps().size() - 1; i >= 0; i--) {
            Step s = super.getSteps().getSteps().get(i);
            if (s.getAction().isPresent() && s.getAction().get().getInputDataShape().isPresent()
                    && s.getAction().get().getOutputDataShape().isPresent()) {
                previousStepWithDatashapes = super.getSteps().getSteps().get(i);
                break;
            }
        }

        if (previousStepWithDatashapes == null) {
            fail("Unable to find previous step with both datashapes set");
        }

        StepDescriptor sd = stepDescriptorEndpoint.getStepDescriptor("aggregate",
            previousStepWithDatashapes.getAction().get().getInputDataShape().get(),
            previousStepWithDatashapes.getAction().get().getOutputDataShape().get()
        );

        super.addProperty(StepProperty.KIND, StepKind.aggregate);
        super.addProperty(StepProperty.ACTION, super.generateStepAction(previousStepWithDatashapes.getAction().get(), sd));
        super.addProperty(StepProperty.STEP_NAME, "Aggregate");
        super.createStep();
    }
}
