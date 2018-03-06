package io.syndesis.qe.steps.monitoring;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.integrations.detail.IntegrationDetailPage;

public class MonitoringSteps {

    private IntegrationDetailPage detailPage = new IntegrationDetailPage();

    @And("^expands \"([^\"]*)\" activity row$")
    public void expandsActivityRow(String order) {
        int ord;
        switch (order) {
            case "first":
                ord = 0;
                break;
            case "second":
                ord = 1;
                break;
            default:
                ord = 0;
                break;
        }

        detailPage.getIntegrationActivityListComponent().clickOnActivity(ord);
    }

    @Then("^clicks on the \"([^\"]*)\" Button of \"([^\"]*)\" activity step$")
    public void clicksOnTheButtonOfStep(String buttonName, String activityStepName) {
        detailPage.getIntegrationActivityListComponent().clickButtonOfActivityStep(buttonName, activityStepName);
    }

    @Then("^she is presented with the activity log$")
    public void sheIsPresentedWithTheLog() {
        detailPage.getIntegrationActivityListComponent().checkLogIsPresent();
    }

    @And("^validates that this log represents rest endpoint log of \"([^\"]*)\" and \"([^\"]*)\" activity step$")
    public void validatesThatThisLogRepresentsRestEndpointLogOfAndStep(String integrationName, String activityStepName) {
        detailPage.getIntegrationActivityListComponent().checkLogIsValid(integrationName, activityStepName);
    }


    @Then("^selects the \"([^\"]*)\" till \"([^\"]*)\" dates in calendar$")
    public void selectsDatesInCalendar(String startDate, String endDate) {
        detailPage.getIntegrationActivityListComponent().setCalendar(startDate, endDate);
    }

}
