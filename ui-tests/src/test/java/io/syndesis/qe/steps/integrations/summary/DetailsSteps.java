package io.syndesis.qe.steps.integrations.summary;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.steps.integrations.IntegrationSteps;
import io.syndesis.qe.steps.other.InvokeHttpRequest;

import org.assertj.core.api.Assertions;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetailsSteps {

    private Details detailPage = new Details();
    private IntegrationSteps integrationSteps = new IntegrationSteps();

    @Then("^check visibility of \"([^\"]*)\" integration details$")
    public void verifyIntegrationDetails(String integrationName) {
        log.info("Integration detail editPage must show integration name");
        assertThat(detailPage.getIntegrationName()).isEqualTo(integrationName);
    }

    @When("^delete the integration on detail page.*$")
    public void deleteIntegrationOnDetailPage() {
        detailPage.deleteIntegration();
    }

    @Then("^check visibility of \"([^\"]*)\" integration status on Integration Detail page$")
    public void checkStatusOnIntegrationDetail(String expectedStatus) {
        String status = detailPage.getIntegrationInfo();
        log.info("Status: {}", status);
        assertThat(status.contains(expectedStatus)).isTrue();
    }

    @When("^click on the \"([^\"]*)\" tab$")
    public void clicksOnTheTab(String tabName) {
        detailPage.selectTab(tabName);
    }

    @Then("^check starting integration status on Integration Detail page$")
    public void checkStartingStatusOnIntegrationDetail() {
        integrationSteps.checkStartingStatus("", "Integration detail");
    }

    @Then("check that webhook url for \"([^\"]*)\" with token \"([^\"]*)\" in UI is same as in routes$")
    public void checkWebhookUrl(String nameOfIntegration, String token) {
        String url = InvokeHttpRequest.getUrlForWebhook(nameOfIntegration, token);
        assertThat(detailPage.getApiUrl()).isEqualToIgnoringCase(url);
    }

    @Then("^verify the displayed webhook URL matches regex (.*)$")
    public void verifyWebhookUrl(String regex) {
        String apiUrl = new Details().getApiUrl();
        Assertions.assertThat(apiUrl).matches(regex);
    }
}
