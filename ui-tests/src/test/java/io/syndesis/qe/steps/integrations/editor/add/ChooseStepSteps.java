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
        switch (stepName) {
            case "Log": //workaround, two logs exist as step. Before change, in the test  was used integration log step for middle step logging.
                chooseStep.chooseStepByDescription("Send a message to the integration\\'s log.");
                break;
            case "Data Mapper":
                chooseStep.chooseStep(stepName);
                chooseStep.waitForStepAppears(By.className("dataMapperBody"), 10000);
                break;
            default:
                chooseStep.chooseStep(stepName);
        }
    }

}
