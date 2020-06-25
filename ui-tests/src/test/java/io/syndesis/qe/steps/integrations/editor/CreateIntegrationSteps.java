package io.syndesis.qe.steps.integrations.editor;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.integrations.editor.CreateIntegration;
import io.syndesis.qe.utils.TestUtils;

import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
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
        new Form(createIntegration.getRootElement()).fillByTestId(data.asMap(String.class, String.class));
        TestUtils.sleepForJenkinsDelayIfHigher(1);
    }
}
