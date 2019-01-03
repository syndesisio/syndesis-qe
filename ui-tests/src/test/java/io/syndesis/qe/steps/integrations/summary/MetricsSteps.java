package io.syndesis.qe.steps.integrations.summary;

import com.codeborne.selenide.Selenide;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.pages.integrations.summary.Metrics;
import io.syndesis.qe.utils.CalendarUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MetricsSteps {

    private Metrics metricsTab = new Details().getMetricsTab();

    public void refresh() {
        Selenide.refresh();
    }

    @Autowired
    private CalendarUtils calendarUtils;

    @Then("^check that number of total error is (\\w+)$")
    public void checkNumberOfTotalError(int numberOfErrors) {
        refresh();
        Assertions.assertThat(metricsTab.getTotalErrors()).isEqualTo(numberOfErrors);
    }

    /***
     * Check that last processed is valid and it is between start and end time of request
     */
    @Then("^check that last processed date is valid$")
    public void checkLastProcessed() throws ParseException {
        refresh();
        String dateLabel = metricsTab.getLastProcessed();
        Assertions.assertThat(dateLabel).matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}, \\d{2}:\\d{2}$");

        Date dateWhenProcessed = new SimpleDateFormat("MM/dd/yyyy, HH:mm").parse(dateLabel);
        Calendar processedCalendar = Calendar.getInstance();
        processedCalendar.setTime(dateWhenProcessed);

        /*clear milliseconds and seconds because UI last processed label contains only minutes*/
        Calendar beforeRequest = calendarUtils.getBeforeRequest();
        beforeRequest.clear(Calendar.MILLISECOND);
        beforeRequest.clear(Calendar.SECOND);
        Calendar afterRequest = calendarUtils.getAfterRequest();
        afterRequest.clear(Calendar.MILLISECOND);
        afterRequest.clear(Calendar.SECOND);

        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (!processedCalendar.equals(beforeRequest)) { //if not same, test whether it was after
            Assertions.assertThat(processedCalendar.after(beforeRequest))
                    .as("Check that processed date and time \"%s\" is after %s",
                            outFormat.format(processedCalendar.getTime()),
                            outFormat.format(beforeRequest.getTime()))
                    .isTrue();
        }
        if (!processedCalendar.equals(afterRequest)) { //if not same, test whether it was before
            Assertions.assertThat(processedCalendar.before(afterRequest))
                    .as("Check that processed date and time \"%s\" is before %s",
                            outFormat.format(processedCalendar.getTime()),
                            outFormat.format(afterRequest.getTime()))
                    .isTrue();
        }
    }

    @Then("^check that number of valid messages is (\\w+)$")
    public void checkNumberOfValidMessages(int numberOfValidMessages) {
        refresh();
        Assertions.assertThat(metricsTab.getNumberOfValidMessages()).isEqualTo(numberOfValidMessages);
    }

    @Then("^check that number of error messages is (\\w+)$")
    public void checkNumberOfErrorMessages(int numberOfErrorMessages) {
        refresh();
        Assertions.assertThat(metricsTab.getNumberOfErrorMessages()).isEqualTo(numberOfErrorMessages);
    }

    @Then("^check that number of total messages is (\\w+)$")
    public void checkNumberOfTotalMessages(int numberOfTotalMessages) {
        refresh();
        Assertions.assertThat(metricsTab.getNumberOfTotalMessages()).isEqualTo(numberOfTotalMessages);
    }

    @Then("^check that up time is ([^\"]*)$")
    public void checkUpTime(String upTime) {
        refresh();
        Assertions.assertThat(metricsTab.getUpTime()).contains(upTime);
    }

}
