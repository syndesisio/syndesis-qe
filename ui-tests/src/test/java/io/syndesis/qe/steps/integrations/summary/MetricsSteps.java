package io.syndesis.qe.steps.integrations.summary;

import com.codeborne.selenide.Selenide;
import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.pages.integrations.summary.Metrics;
import io.syndesis.qe.utils.CalendarUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.within;

@Slf4j
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

        if (!processedCalendar.equals(beforeRequest)) { //if not same then it tests whether it was after
            Assertions.assertThat(processedCalendar.after(beforeRequest))
                    .as("Check that processed date and time \"%s\" is after %s",
                            outFormat.format(processedCalendar.getTime()),
                            outFormat.format(beforeRequest.getTime()))
                    .isTrue();
        }
        if (!processedCalendar.equals(afterRequest)) { //if not same then it tests whether it was before
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

    @Then("^check that uptime for ([^\"]*) pod is valid$")
    public void checkUptime(String integration) throws ParseException, InterruptedException {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(integration);
        if (pod.isPresent()) {
            refresh();
            // To minimize inaccuracy they should be called together
            String uptime = metricsTab.getUpTime();
            String openshiftTime = pod.get().getStatus().getStartTime();

            if (uptime.contains("n/a")) {
                Thread.sleep(10000);
                refresh();
                uptime = metricsTab.getUpTime();
                openshiftTime = pod.get().getStatus().getStartTime();
            }

            Pattern pattern = Pattern.compile("^(\\d{1,2}) minutes$");
            Matcher matcher = pattern.matcher(uptime);
            if (!matcher.find()) {
                Assertions.fail("UI uptime " + uptime + " doesn't match pattern.");
            }
            Long uiMinute = Long.valueOf(matcher.group(1));

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date openShiftStartDate = format.parse(openshiftTime);
            long diff = new Date().getTime() - openShiftStartDate.getTime();
            long openShiftMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            Assertions.assertThat(uiMinute)
                    .as("Check that UI uptime '%s' is same as openshift uptime '%s'.",
                            uiMinute, openShiftMinutes)
                    .isCloseTo(openShiftMinutes, within(Long.valueOf(1))); //1-second inaccuracy can happen
        } else {
            Assertions.fail("Pod with name " + integration + " doesn't exist!");
        }
    }

    /*e.g. Since Dec 19th 10:42*/
    @Then("^check that startdate for ([^\"]*) pod is valid$")
    public void checkDateFrom(String integration) throws ParseException, InterruptedException {
        String startTime = metricsTab.getStartTime();
        Date uiStartDate = parseUiSinceDate(startTime);

        // UI date and start date cannot be same, Issue: gh-4303
        int timeout = 0;
        while (isSameAsCurrentDate(uiStartDate)) {
            timeout++;
            log.info("UI time is same as actual, probably the issue: gh-4303. Waiting 30 seconds and refresh UI. " + timeout + ". attempt.");
            Thread.sleep(30000);
            refresh();
            startTime = metricsTab.getStartTime();
            uiStartDate = parseUiSinceDate(startTime);
            if (timeout == 10) {
                Assertions.fail("UI time is same as actual time which is impossible");
            }
        }

        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(integration);
        if (pod.isPresent()) {
            String openshiftTime = pod.get().getStatus().getStartTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date openshiftStartDate = format.parse(openshiftTime);

            Calendar uiDate = Calendar.getInstance();
            uiDate.setTime(uiStartDate);
            uiDate.clear(Calendar.SECOND);

            Calendar openshiftDate = Calendar.getInstance();
            openshiftDate.setTime(openshiftStartDate);
            // because UI doesn't contains seconds and minutes are rounded e.g. 8:23:42 is in UI 8:24
            openshiftDate = DateUtils.round(openshiftDate, Calendar.MINUTE);
            openshiftDate.clear(Calendar.SECOND);

            Assertions.assertThat(uiDate.getTime())
                    .as("Check that UI date of pod '%s' is same as date in the openshift '%s'.",
                            startTime, openshiftTime)
                    .isCloseTo(openshiftDate.getTime(), 60000L); // UI sometimes round time UP sometimes not
        } else {
            Assertions.fail("Pod with name " + integration + " doesn't exist!");
        }
    }

    /**
     * Parse since date to the Date
     * e.g. Since Dec 19th 10:42 -> group1 = Dec 19 group2= 10:42
     */
    private Date parseUiSinceDate(String dateLable) throws ParseException {
        Pattern pattern = Pattern.compile("^.* (\\w{3,4} \\d{1,2})\\w{2} (\\d{2}:\\d{2})$");
        Matcher matcher = pattern.matcher(dateLable);
        if (!matcher.find()) {
            Assertions.fail("Date " + dateLable + " doesn't match pattern.");
        }
        // year have to be added, UI doesn't contain it
        return new SimpleDateFormat("MMM dd HH:mm yyyy")
                .parse(matcher.group(1) + " " + matcher.group(2) + " " + Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * Test whether UI date is same as current date.
     */
    private boolean isSameAsCurrentDate(Date uiStartDate) {
        Calendar uiDate = Calendar.getInstance();
        uiDate.setTime(uiStartDate);
        uiDate.clear(Calendar.SECOND);

        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        currentDate.clear(Calendar.SECOND);
        currentDate.clear(Calendar.MILLISECOND);

        return uiDate.getTime().compareTo(currentDate.getTime()) == 0;
    }

}
