package io.syndesis.qe.steps.connections.wizard;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.connections.wizard.ConnectionWizard;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionWizardSteps {

    @Autowired
    private ConnectionWizard wizard;

    @When("^(\\w+) navigates to the next Connection wizard step \"([^\"]*)\"$")
    public void navigateToNextWizardStep(String user, String step) {
        wizard.nextStep();

        //validation ensures that multiple clicking the Next button doesn't click on the same button multiple times before the next step loads and displays
        wizard.getCurrentStep().validate();
    }

    @When("^(\\w+) creates new connection$")
    public void finishNewConnectorWizard(String user) {
        wizard.nextStep();
    }
}
