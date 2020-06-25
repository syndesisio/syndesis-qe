package io.syndesis.qe.steps.integrations.editor.add;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.pages.connections.fragments.list.ConnectionsList;
import io.syndesis.qe.pages.integrations.editor.add.ChooseStep;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.concurrent.TimeoutException;

import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseStepSteps {

    private ChooseStep chooseStep = new ChooseStep();
    private ConnectionsList connectionsList = new ConnectionsList(null);

    @When("^select \"([^\"]*)\" integration step$")
    public void chooseStep(String stepName) {
        log.info("Adding {} step to integration", stepName);
        try {
            OpenShiftWaitUtils.waitFor(() -> chooseStep.validate(), 1000 * 30L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Choose step page was not loaded in 30s!", e);
        }
        connectionsList.getItem(stepName).click();
    }
}
