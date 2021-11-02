package io.syndesis.qe.steps.integrations.editor;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.integrations.editor.CreateIntegration;
import io.syndesis.qe.utils.TestUtils;

import java.util.List;
import java.util.Map;

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
    public void checkLabels(DataTable expectedLabels) {
        List<String> allLabels = createIntegration.getAllLabels();
        assertThat(allLabels).containsExactlyInAnyOrderElementsOf(expectedLabels.cells().get(0));
    }

    @When("^add environment variables")
    public void setIntegrationEnvs(DataTable envs) {
        for (List<String> dataRow : envs.cells()) {
            createIntegration.addEnvironmentVariable(dataRow.get(0), dataRow.get(1));
        }
    }

    @When("delete environment variable {string}")
    public void deleteIntegrationEnv(String integrationEnv) {
        createIntegration.deleteEnvironmentVariable(integrationEnv);
    }

    @When("update environment {string} variable to {string}")
    public void updateIntegrationEnvValue(String integrationEnv, String newValue) {
        createIntegration.updateEnvironmentVariable(integrationEnv, newValue);
    }

    @When("update environment {string} name to {string}")
    public void updateIntegrationEnvName(String integrationEnv, String newName) {
        createIntegration.updateEnvironmentName(integrationEnv, newName);
    }

    @Then("^check that integration contains environment variables and they values")
    public void checkEnvs(DataTable expectedEnvs) {
        Map<String, String> envsAndValues = createIntegration.getAllEnvironmentVariablesAndTheyValues();
        assertThat(envsAndValues).containsAllEntriesOf(expectedEnvs.asMap(String.class, String.class));
    }
}
