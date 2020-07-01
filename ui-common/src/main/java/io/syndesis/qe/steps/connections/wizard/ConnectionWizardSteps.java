package io.syndesis.qe.steps.connections.wizard;

import io.cucumber.java.en.When;
import io.syndesis.qe.pages.connections.wizard.ConnectionWizard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConnectionWizardSteps {

    @Autowired
    private ConnectionWizard wizard;

    @When("^navigate to the next Connection wizard step \"([^\"]*)\"$")
    public void navigateToNextWizardStep(String step) {
        wizard.nextStep();

        //validation ensures that multiple clicking the Next button doesn't click on the same button multiple times before the next step loads and displays
        wizard.getCurrentStep().validate();
    }

    @When("^create new connection$")
    public void finishNewConnectorWizard() {
        wizard.nextStep();
    }
}
