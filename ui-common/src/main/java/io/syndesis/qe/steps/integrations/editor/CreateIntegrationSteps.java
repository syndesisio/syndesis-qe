package io.syndesis.qe.steps.integrations.editor;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.integrations.editor.CreateIntegration;
import io.syndesis.qe.utils.TestUtils;

import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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

    @When("^add integration label \"([^\"]*)\"$")
    public void setIntegrationLabel(String integrationLabel) {
        createIntegration.addLabel(integrationLabel);
    }

    @When("^delete integration label \"([^\"]*)\"$")
    public void deleteIntegrationLabel(String integrationLabel) {
        createIntegration.deleteLabel(integrationLabel);
    }

    @Then("^check that integration contains labels$")
    public void checkAlertDialog(DataTable expectedLabels) {
        List<String> allLabels = createIntegration.getAllLabels();
        assertThat(allLabels).containsExactlyInAnyOrderElementsOf(expectedLabels.cells().get(0));
    }
}
