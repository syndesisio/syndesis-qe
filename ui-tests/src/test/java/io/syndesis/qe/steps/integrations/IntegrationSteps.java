package io.syndesis.qe.steps.integrations;


import com.codeborne.selenide.SelenideElement;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.integrations.IntegrationStartingStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.visible;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;

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
    @Then("^check starting integration ([^\"]*) status on Integrations page$")
    public void checkStartingStatusOnIntegrationsPage( String integrationName) {
        checkStartingStatus(integrationName, "Integrations");
    }

    //base starting status checking method used in step methods
    public void checkStartingStatus( String integrationName, String checkedPage) {
        List<IntegrationStartingStatus> statuses = new ArrayList<>();
        statuses.add(IntegrationStartingStatus.ASSEMBLING);
        statuses.add(IntegrationStartingStatus.BUILDING);
        statuses.add(IntegrationStartingStatus.DEPLOYING);
        statuses.add(IntegrationStartingStatus.STARTING);

        int lastStatusIndex = 0;
        int matchingStatesNumber = 0;
        StringBuilder statusesMessage = new StringBuilder("");
        String lastStatus = "";

        //polling every 200 ms for 10 minutes
        for (int i = 0; i < 5 * 60 * 10; i++) {
            if (lastStatusIndex == statuses.size() - 1) {
                switch (checkedPage) {
                    case "Home":
                    case "Integrations":
                        log.info("Status changed to: " + integrations.getIntegrationItemStatus(integrations.getIntegration(integrationName)).trim());
                        Assertions.assertThat(integrations.getIntegrationItemStatus(integrations.getIntegration(integrationName)).trim()).isEqualToIgnoringWhitespace("Running");
                        break;
                    case "Integration detail":
                        log.info("Status changed to: " + detailPage.getPublishedVersion().getText().trim());
                        Assertions.assertThat(detailPage.getPublishedVersion().getText()).isEqualToIgnoringWhitespace("Published version 1");
                        break;
                    default:
                        Assert.fail("Integration status can't be checked on <" + checkedPage + "> page. Only valid options are [Integrations, Integration detail, Home]");
                }
                break;
            }

            String status = "";
            try {
                switch (checkedPage) {
                    case "Home":
                    case "Integrations":
                        status = integrations.getIntegrationItemStartingStatus(integrations.getIntegration(integrationName));
                        break;
                    case "Integration detail":
                        status = detailPage.getStartingStatus();
                        break;
                    default:
                        Assert.fail("Integration status can't be checked on <" + checkedPage + "> page. Only valid options are [Integrations, Integration detail, Home]");
                }
            } catch(Throwable t) {
                lastStatusIndex = statuses.size()-1;
                continue;
            }

            if (!lastStatus.equals(status)) {
                lastStatus = status;
                statusesMessage.append(" " + status);
            }

            for (int j = lastStatusIndex; j < statuses.size(); j++) {
                if (statuses.get(j).getStatus().equals(status)) {
                    if (matchingStatesNumber == 0) {
                        matchingStatesNumber++;
                        lastStatusIndex = j;
                        log.info("Status changed to: " + status);
                    } else {
                        if (lastStatusIndex < j) {
                            matchingStatesNumber++;
                            lastStatusIndex = j;
                            log.info("Status changed to: " + status);
                        }
                    }
                }
                else {}
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Assertions.assertThat(matchingStatesNumber).isGreaterThanOrEqualTo(2).withFailMessage("Spotted statuses' order:" + statusesMessage.toString());
    }
}
