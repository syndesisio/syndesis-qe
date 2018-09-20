package io.syndesis.qe.steps.integrations.editor.add;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.editor.add.ChooseStep;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class ChooseStepSteps {

    private ChooseStep chooseStep = new ChooseStep();

    @When("^select \"([^\"]*)\" integration step$")
    public void chooseStep(String stepName) {
        log.info("Adding {} step to integration", stepName);
        try {
            OpenShiftWaitUtils.waitFor(() -> chooseStep.validate(), 1000 * 30L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Choose step page was not loaded in 30s!", e);
        }
        chooseStep.chooseStep(stepName);
    }

}
