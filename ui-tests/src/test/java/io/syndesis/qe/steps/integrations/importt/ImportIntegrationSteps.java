package io.syndesis.qe.steps.integrations.importt;

import cucumber.api.java.en.And;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.integrations.Integrations;
import io.syndesis.qe.pages.integrations.importt.ImportIntegration;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.io.File;

@Slf4j
public class ImportIntegrationSteps {

    private Integrations integrations = new Integrations();
    private ImportIntegration importIntegrationPage = new ImportIntegration();

    @And("^import integration \"([^\"]*)\"$")
    public void importIntegration(String integrationName) throws InterruptedException {
        importIntegrationPage.importIntegration(integrationName);
        //give jenkins more time so the integration shows up in the list
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay() * 1000);
        Assertions.assertThat(integrations.isIntegrationPresent(integrationName)).isTrue();
    }

    @And("^drag exported integration \"([^\"]*)\" file to drag and drop area$")
    public void importIntegrationViaDragAndDrop(String integrationName) throws InterruptedException {
        importIntegrationPage.importIntegrationViaDragAndDrop(integrationName);
        Assertions.assertThat(integrations.isIntegrationPresent(integrationName)).isTrue();
    }

    @And("^import integration from relative file path \"([^\"]*)\"$")
    public void importIntegrationFromFile(String stringPathToFile) throws InterruptedException {
        importIntegrationPage.importIntegration(new File(stringPathToFile));
        //give jenkins more time so the integration shows up in the list
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay());
    }
}
