package io.syndesis.qe.steps.integrations.summary;

import com.codeborne.selenide.Selenide;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.integrations.summary.Activity;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.utils.CalendarUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivitySteps {

    private Activity activityTab = new Details().getActivityTab();

    public void refresh() {
        Selenide.refresh();
    }

    @Autowired
    private CalendarUtils calendarUtils;

    /**
     * @param index from 1 , first activity = 1
     */
    @And("^expands \"(\\w+)\". activity$")
    public void expandsActivityRow(int index) {
        activityTab.clickOnActivity(index);
    }

    @Then("^check that in the activity log are (\\w+) activities$")
    public void checkAllActivities(int numberOfActivities) {
        Assertions.assertThat(activityTab.getAllActivities()).hasSize(numberOfActivities);
    }


    /**
     * Check that activity date and time is between start and end time of request
     *
     * @param index from 1 , first activity = 1
     */
    @Then("^check that (\\w+). activity date and time is valid$")
    public void checkDateAndTime(int index) throws ParseException {
        refresh();
        String timeLabel = activityTab.getActivityTime(index - 1);
        String dateLabel = activityTab.getActivityDate(index - 1);
        Assertions.assertThat(timeLabel)
                .matches("^\\d{2}:\\d{2}:\\d{2}$");
        Assertions.assertThat(dateLabel)
                .matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$");
        Date activityDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(dateLabel + " " + timeLabel);
        checkDateIsMiddle(activityDate);
    }

    /**
     * @param index from 1 , first activity = 1
     */
    @Then("^check that (\\w+). activity version contains ([^\"]*)$")
    public void checkVersion(int index, String version) {
        Assertions.assertThat(activityTab.getActivityVersion(index - 1)).contains(version);
    }

    /**
     * @param index from 1 , first activity = 1
     */
    @Then("^check that (\\w+). activity has error$")
    public void checkActivityError(int index) {
        Assertions.assertThat(activityTab.getActivityError(index - 1)).contains("Errors found");
    }

    /**
     * @param index from 1 , first activity = 1
     */
    @Then("^check that (\\w+). activity has not any errors$")
    public void checkNoErrors(int index) {
        Assertions.assertThat(activityTab.getActivityError(index - 1)).contains("No errors");
    }

    /**
     * @param index from 1 , first activity = 1
     */
    @Then("^check that (\\w+). activity has (\\w+) steps in the log$")
    public void checkNumberOfSteps(int index, int numberOfSteps) {
        Assertions.assertThat(activityTab.getActivityLogRows(index - 1)).hasSize(numberOfSteps);
    }

    /**
     * @param indexRow      from 1 , first row = 1
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that (\\w+). step in the (\\w+). activity contains (.*) in the output$")
    public void checkOutputOfStep(int indexRow, int indexActivity, String output) {
        Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow - 1, Activity.COLUMN.OUTPUT))
                .contains(output);
    }

    /**
     * @param indexRow      from 1 , first row = 1
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that (\\w+). step in the (\\w+). activity is ([^\"]*) step$")
    public void checkNameOfStep(int indexRow, int indexActivity, String nameOfStep) {
        Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow - 1, Activity.COLUMN.STEP))
                .contains(nameOfStep);
    }

    /**
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that all steps in the (\\w+). activity has ([^\"]*) status$")
    public void checkAllStatusOfStep(int indexActivity, String status) {
        int numberOfRow = activityTab.getActivityLogRows(indexActivity - 1).size();
        for (int indexRow = 1; indexRow <= numberOfRow; indexRow++) {
            checkStatusOfStep(indexRow, indexActivity, status);
        }
    }

    /**
     * @param indexRow      from 1 , first row = 1
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that (\\w+). step in the (\\w+). activity has ([^\"]*) status$")
    public void checkStatusOfStep(int indexRow, int indexActivity, String status) {
        Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow - 1, Activity.COLUMN.STATUS))
                .contains(status);
    }

    /**
     * Check that time in step is between start and end time of request
     *
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that all steps in the (\\w+). activity has valid time$")
    public void checkAllTimeOfStep(int indexActivity) throws ParseException {
        int numberOfRow = activityTab.getActivityLogRows(indexActivity - 1).size();
        for (int indexRow = 0; indexRow < numberOfRow; indexRow++) {
            String timeLabel = activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow, Activity.COLUMN.TIME);
            Assertions.assertThat(timeLabel).matches("^\\w{3,4} \\d{1,2}, \\d{4}, \\d{2}:\\d{2}:\\d{2}$");
            Date dateOfStep = new SimpleDateFormat("MMM dd,yyyy, HH:mm:ss").parse(timeLabel);
            checkDateIsMiddle(dateOfStep);
        }
    }

    /**
     * Check whether Date is middle before and after request date
     *
     * @param middle - middle date
     */
    private void checkDateIsMiddle(Date middle) {
        Calendar middleCalendar = Calendar.getInstance();
        middleCalendar.setTime(middle);

        /*clear milliseconds because UI last processed label contains only minutes*/
        Calendar beforeRequest = calendarUtils.getBeforeRequest();
        beforeRequest.clear(Calendar.MILLISECOND);
        Calendar afterRequest = calendarUtils.getAfterRequest();
        afterRequest.clear(Calendar.MILLISECOND);

        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!middleCalendar.equals(beforeRequest)) { //if not same, test whether it was after
            Assertions.assertThat(middleCalendar.after(beforeRequest))
                    .as("Check that activity/step date and time \"%s\" is after %s",
                            outFormat.format(middleCalendar.getTime()),
                            outFormat.format(beforeRequest.getTime()))
                    .isTrue();
        }
        if (!middleCalendar.equals(afterRequest)) { //if not same, test whether it was before
            Assertions.assertThat(middleCalendar.before(afterRequest))
                    .as("Check that activity/step date and time \"%s\" is before %s",
                            outFormat.format(middleCalendar.getTime()),
                            outFormat.format(afterRequest.getTime()))
                    .isTrue();
        }
    }

    /**
     * Check validity of duration via REGEX
     *
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that all steps in the (\\w+). activity has valid duration$")
    public void checkAllDurationOfStep(int indexActivity) {
        int numberOfRow = activityTab.getActivityLogRows(indexActivity - 1).size();
        for (int indexRow = 0; indexRow < numberOfRow; indexRow++) {
            Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow, Activity.COLUMN.DURATION))
                    .matches("^.*(\\d+ ms|\\d+ seconds)$");
        }
    }
}
