package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.utils.TestUtils;

public class LogSteps {
    @Autowired
    private StepsStorage steps;

    @Given("^add log step$")
    public void addLogStep() {
        final Step basicFilter = new Step.Builder()
                .stepKind(StepKind.log)
                .configuredProperties(TestUtils.map("contextLoggingEnabled", "true",
                        "bodyLoggingEnabled", "true"
                ))
                .id(UUID.randomUUID().toString())
                .build();
        steps.getStepDefinitions().add(new StepDefinition(basicFilter));
    }
}
