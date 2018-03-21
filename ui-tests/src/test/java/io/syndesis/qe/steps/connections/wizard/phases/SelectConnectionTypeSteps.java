package io.syndesis.qe.steps.connections.wizard.phases;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.connections.wizard.ConnectionWizard;
import io.syndesis.qe.pages.connections.wizard.phases.SelectConnectionType;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnection;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnectionAmq;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectConnectionTypeSteps {

    @Autowired
    private SelectConnectionType selectConnectionTypePage;

    @Autowired
    private ConnectionWizard wizard;

    @When("^Camilla selects \"([^\"]*)\" connection type$")
    public void selectConnectionType(String connectionName) {
        selectConnectionTypePage.selectConnectionType(connectionName);

        ConfigureConnection connectionWizardStep = null;
        switch(connectionName) {
            case "AMQ": connectionWizardStep = new ConfigureConnectionAmq();
                break;
            default:
                connectionWizardStep = new ConfigureConnection();
        }

        wizard.replaceStep(connectionWizardStep,1);
    }
}
