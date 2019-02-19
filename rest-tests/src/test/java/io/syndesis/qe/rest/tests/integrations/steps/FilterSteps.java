package io.syndesis.qe.rest.tests.integrations.steps;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.filter.FilterPredicate;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.utils.FilterRulesBuilder;
import io.syndesis.qe.utils.TestUtils;

public class FilterSteps {
    @Autowired
    private StepsStorage steps;

    @Given("^create basic TW to SF filter step")
    public void createBasicFilterStep() {
        try {
            this.createBasicFilterStepWord("text", "#backendTest", "contains");
        } catch (Throwable throwable) {
            Assertions.fail(throwable.getMessage());
        }
    }

    @Given("^create basic filter step for \"([^\"]*)\" with word \"([^\"]*)\" and operation \"([^\"]*)\"")
    public void createBasicFilterStepWord(String path, String value, String operation) {
        final Step basicFilter = new Step.Builder()
                .stepKind(StepKind.ruleFilter)
                .configuredProperties(TestUtils.map(
                        "type", "rule",
                        "predicate", FilterPredicate.AND.toString(),
                        "rules", new FilterRulesBuilder().addPath(path).addValue(value).addOps(operation).build()
                ))
                .id(UUID.randomUUID().toString())
                .name("Rule Filter " + path)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(basicFilter));
    }
}
