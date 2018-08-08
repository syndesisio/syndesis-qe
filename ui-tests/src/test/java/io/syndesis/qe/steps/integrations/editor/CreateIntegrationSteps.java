package io.syndesis.qe.steps.integrations.editor;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.integrations.editor.CreateIntegration;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateIntegrationSteps {

    private CreateIntegration createIntegration = new CreateIntegration();

    @When("^set integration name \"([^\"]*)\"$")
    public void setIntegrationName(String integrationName) {
        createIntegration.setName(integrationName);
    }

    @When("^fills? Name Integration form$")
    public void fillNameConnectionForm(DataTable data) {
        new Form(createIntegration.getRootElement()).fillByLabel(data.asMap(String.class, String.class));
        TestUtils.sleepForJenkinsDelayIfHigher(1);
    }
}
