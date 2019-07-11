package io.syndesis.qe.steps.integrations.importt;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.integrations.Integrations;
import io.syndesis.qe.pages.integrations.importt.ImportIntegration;
import io.syndesis.qe.utils.TestUtils;

import java.io.File;

import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportIntegrationSteps {

    private Integrations integrations = new Integrations();
    private ImportIntegration importIntegrationPage = new ImportIntegration();

    @When("^import integration \"([^\"]*)\"$")
    public void importIntegration(String integrationName) {
        importIntegrationPage.importIntegration(integrationName);
        TestUtils.sleepForJenkinsDelayIfHigher(3);
    }

    @When("^drag exported integration \"([^\"]*)\" file to drag and drop area$")
    public void importIntegrationViaDragAndDrop(String integrationName) {
        importIntegrationPage.importIntegrationViaDragAndDrop(integrationName);
    }

    @When("^import integration from relative file path \"([^\"]*)\"$")
    public void importIntegrationFromFile(String stringPathToFile) {
        importIntegrationPage.importIntegration(new File(stringPathToFile));
        //give jenkins more time so the integration shows up in the list
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay());
    }
}
