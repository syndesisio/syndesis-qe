package io.syndesis.qe.steps.integrations.editor.add;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.editor.add.ChooseStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseStepSteps {

    private ChooseStep chooseStep = new ChooseStep();

    @When("^select \"([^\"]*)\" integration step$")
    public void chooseStep(String stepName) {
        log.info("Adding {} step to integration", stepName);
        chooseStep.chooseStep(stepName);
    }

}
