package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.syndesis.common.model.filter.FilterPredicate;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.utils.FilterRulesBuilder;
import io.syndesis.qe.utils.TestUtils;

public class FilterSteps extends AbstractStep{
    @When("^create basic filter step for \"([^\"]*)\" with word \"([^\"]*)\" and operation \"([^\"]*)\"")
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
}
