package io.syndesis.qe.steps.integrations.summary;

import io.syndesis.qe.pages.integrations.summary.Activity;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.utils.CalendarUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.Selenide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

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
    @When("^expands \"(\\w+)\". activity$")
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
    @Then("^check that (\\w+). activity date and time is valid with (\\w+) second accuracy$")
    public void checkDateAndTime(int index, int accuracy) throws ParseException {
        refresh();
        String timeLabel = activityTab.getActivityTime(index - 1);
        String dateLabel = activityTab.getActivityDate(index - 1);
        Assertions.assertThat(timeLabel)
            .matches("^\\d{1,2}:\\d{2}:\\d{2} (AM|PM)$");
        Assertions.assertThat(dateLabel)
            .matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$");
        Date activityDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(dateLabel + " " + timeLabel);
        checkDateIsMiddle(activityDate, accuracy);
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
        Assertions.assertThat(activityTab.getActivityError(index - 1)).contains("ErrorsFound"); //gh-5721
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
     * @param indexRow from 1 , first row = 1
     * @param indexActivity from 1 , first activity = 1
     */
    @Then("^check that (\\w+). step in the (\\w+). activity contains (.*) in the output$")
    public void checkOutputOfStep(int indexRow, int indexActivity, String output) {
        Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow - 1, Activity.COLUMN.OUTPUT))
            .contains(output);
    }

    /**
     * @param indexRow from 1 , first row = 1
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
     * @param indexRow from 1 , first row = 1
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
    @Then("^check that all steps in the (\\w+). activity has valid time with (\\w+) second accuracy$")
    public void checkAllTimeOfStep(int indexActivity, int accuracy) throws ParseException {
        int numberOfRow = activityTab.getActivityLogRows(indexActivity - 1).size();
        for (int indexRow = 0; indexRow < numberOfRow; indexRow++) {
            String timeLabel = activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow, Activity.COLUMN.TIME);
            Assertions.assertThat(timeLabel).matches("^\\d{1,2}/\\d{1,2}/\\d{4}, \\d{1,2}:\\d{1,2}:\\d{2} (AM|PM)$");
            Date dateOfStep = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss").parse(timeLabel);
            checkDateIsMiddle(dateOfStep, accuracy);
        }
    }

    /**
     * Check whether Date is middle before and after request date
     *
     * @param middle - middle date
     * @param accuracy - accuracy for after request in seconds, the integration process is asynchronous with test and
     * it can take some time.
     * e.g. after request can be saved before webhook message goes through all integration.
     */
    private void checkDateIsMiddle(Date middle, int accuracy) {
        Calendar middleCalendar = Calendar.getInstance();
        middleCalendar.setTime(middle);

        /*clear milliseconds because UI last processed label contains only minutes*/
        Calendar beforeRequest = calendarUtils.getBeforeRequest();
        beforeRequest.clear(Calendar.MILLISECOND);
        Calendar afterRequest = calendarUtils.getAfterRequest();
        afterRequest.clear(Calendar.MILLISECOND);
        afterRequest.add(Calendar.SECOND, accuracy);

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
                .matches("^.*(\\d+ ms|\\d+ seconds)|(NaN)$"); // NaN is workaround for gh-5715
            //          prepared reproducer for gh-5715
            //          Assertions.assertThat(activityTab.getColumnInRowInActivityLog(indexActivity - 1, indexRow, Activity.COLUMN.DURATION))
            //              .doesNotMatch("^.*(NaN)$");
        }
    }
}
