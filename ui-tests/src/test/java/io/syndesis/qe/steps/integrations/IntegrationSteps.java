package io.syndesis.qe.steps.integrations;


import com.codeborne.selenide.SelenideElement;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.integrations.Integrations;
import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.utils.ExportedIntegrationJSONUtil;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.visible;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class IntegrationSteps {

    private Details detailPage = new Details();
    private Integrations integrations = new Integrations();
    private DataMapper dataMapper = new DataMapper();

    @When("^select the \"([^\"]*)\" integration.*$")
    public void selectIntegration(String itegrationName) {
        integrations.goToIntegrationDetail(itegrationName);
    }

    @When("^delete the \"([^\"]*)\" integration.*$")
    public void deleteIntegration(String integrationName) {
        integrations.deleteIntegration(integrationName);
    }

    @Then("^Integration \"([^\"]*)\" is present in integrations list$")
    public void expectIntegrationPresent(String name) {
        log.info("Verifying integration {} is present", name);
        TestUtils.sleepForJenkinsDelayIfHigher(4);
        assertThat(integrations.isIntegrationPresent(name), is(true));
    }

    @Then("^check that integration \"([^\"]*)\" is not visible$")
    public void expectIntegrationNotPresent(String name) {
        // DOES NOT WORK - THROWS ERROR AS getElement has assert that its > 0 and if it does not exists its 0 TODO
        log.info("Verifying if integration {} is present", name);
        assertThat(integrations.isIntegrationPresent(name), is(false));
    }

    @Then("^wait until integration \"([^\"]*)\" gets into \"([^\"]*)\" state$")
    public void waitForIntegrationState(String integrationName, String integrationStatus) {
        SelenideElement integration = integrations.getIntegration(integrationName);
        assertTrue(TestUtils.waitForEvent(status -> status.equals(integrationStatus), () -> integrations.getIntegrationItemStatus(integration),
                TimeUnit.MINUTES, 10, TimeUnit.SECONDS, 1));
    }

    //Kebab menu test, #553 -> part #548, #549.
    @When("^click on the kebab menu icon of each available Integration and checks whether menu is visible and has appropriate actions$")
    public void clickOnAllKebabMenus() {
        integrations.checkAllIntegrationsKebabButtons();
    }


    @And("^export the integraion$")
    public void exportIntegration() throws InterruptedException {
        File exportedIntegrationFile = detailPage.exportIntegration();
        Assertions.assertThat(exportedIntegrationFile)
                .exists()
                .isFile()
                .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));
        ExportedIntegrationJSONUtil.testExportedFile(exportedIntegrationFile);
    }

    @And("^start integration \"([^\"]*)\"$")
    public void startIntegration(String integrationName) {
        detailPage.clickOnKebabMenuAction("Start");
        ModalDialogPage modal = new ModalDialogPage();
        modal.getButton("Start").shouldBe(visible).click();
    }

    @And("^Wait until there is no integration pod with name \"([^\"]*)\"$")
    public void waitForIntegrationPodShutdown(String integartionPodName) throws InterruptedException {
        OpenShiftWaitUtils.assertEventually("Pod with name " + integartionPodName + "is still running.",
                OpenShiftWaitUtils.areNoPodsPresent(integartionPodName), 1000, 5 * 60 * 1000);
    }




    @And("^.*check that data bucket \"([^\"]*)\" is available$")
    public void checkPreviousDataBuckets(String bucket) {
        //there is condition for element to be visible
        dataMapper.getDataBucketElement(bucket);
    }

    @And("^.*open data bucket \"([^\"]*)\"$")
    public void openDataBucket(String bucket) {
        //check if it exists included
        dataMapper.openBucket(bucket);
    }

    @And("^.*close data bucket \"([^\"]*)\"$")
    public void closeDataBucket(String bucket) {
        dataMapper.closeBucket(bucket);
    }

    @And("^.*perform action with data bucket")
    public void performActionWithBucket(DataTable table) {
        List<List<String>> rows = table.cells(0);
        String action;

        for (List<String> row : rows) {
            action = row.get(1);
            if (action.equalsIgnoreCase("open")) {
                dataMapper.openBucket(row.get(0));
            } else if (action.equalsIgnoreCase("close")) {
                dataMapper.closeBucket(row.get(0));
            } else {
                //check if exists, condition visible is in used method
                dataMapper.getDataBucketElement(row.get(0));
            }
        }
    }

    @Then("^.*validate that logs of integration \"([^\"]*)\" contains string \"([^\"]*)\"$")
    public void checkThatLogsContain(String integrationName, String text) {
        Assertions.assertThat(OpenShiftUtils.getIntegrationLogs(integrationName)).containsIgnoringCase(text);
    }
}
