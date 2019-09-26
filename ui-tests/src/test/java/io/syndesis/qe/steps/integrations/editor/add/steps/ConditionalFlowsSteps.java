package io.syndesis.qe.steps.integrations.editor.add.steps;

import io.syndesis.qe.pages.integrations.editor.add.steps.ConditionalFlows;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalFlowsSteps {

    private final ConditionalFlows conditionalFlows = new ConditionalFlows();

    @Then("^Add another condition$")
    public void addAnotherCondition() {
        conditionalFlows.getAddAnotherConditionButton().click();
    }
}

