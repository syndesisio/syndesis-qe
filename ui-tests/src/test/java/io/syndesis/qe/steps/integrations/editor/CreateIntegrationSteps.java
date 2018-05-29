package io.syndesis.qe.steps.integrations.editor;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.editor.CreateIntegration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateIntegrationSteps {

    private CreateIntegration createIntegration = new CreateIntegration();

    @When("^set integration name \"([^\"]*)\"$")
    public void setIntegrationName(String integrationName) {
        createIntegration.setName(integrationName);
    }
}
