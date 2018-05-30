package io.syndesis.qe.steps.integrations.summary;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.integrations.summary.Details;

public class ActivitySteps {

    private Details detailPage = new Details();

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

    @Then("^click on the \"([^\"]*)\" Button of \"([^\"]*)\" activity step$")
    public void clicksOnTheButtonOfStep(String buttonName, String activityStepName) {
        detailPage.getIntegrationActivityListComponent().clickButtonOfActivityStep(buttonName, activityStepName);
    }

    @Then("^check visibility of the activity log$")
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
