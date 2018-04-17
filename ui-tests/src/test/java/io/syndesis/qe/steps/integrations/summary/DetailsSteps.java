package io.syndesis.qe.steps.integrations.summary;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import org.assertj.core.api.Assertions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.summary.Details;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetailsSteps {

    private Details detailPage = new Details();

    @Then("^Camilla is presented with \"([^\"]*)\" integration details$")
    public void verifyIntegrationDetails(String integrationName) {
        log.info("Integration detail editPage must show integration name");
        assertThat(detailPage.getIntegrationName(), is(integrationName));
    }

    @When("^Camilla deletes the integration on detail page.*$")
    public void deleteIntegrationOnDetailPage() {
        detailPage.deleteIntegration();
    }

    @Then("^she is presented with \"([^\"]*)\" integration status on Integration Detail page$")
    public void checkStatusOnIntegrationDetail(String expectedStatus) {
        String status = detailPage.getIntegrationInfo();
        log.info("Status: {}", status);
        Assertions.assertThat(status.contains(expectedStatus)).isTrue();
    }

    @And("^clicks on the \"([^\"]*)\" tab$")
    public void clicksOnTheTab(String tabName) {
        detailPage.selectTab(tabName);
    }
}
