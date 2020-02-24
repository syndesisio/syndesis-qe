package io.syndesis.qe.steps.integrations.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.pages.integrations.summary.Metrics;
import io.syndesis.qe.utils.CalendarUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.Selenide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsSteps {

    private Metrics metricsTab = new Details().getMetricsTab();

    public void refresh() {
        Selenide.refresh();
        TestUtils.sleepIgnoreInterrupt(2000);
    }

    @Autowired
    private CalendarUtils calendarUtils;

    @Then("^check that number of total error is (\\w+)$")
    public void checkNumberOfTotalError(int numberOfErrors) {
        refresh();
        assertThat(metricsTab.getTotalErrors()).isEqualTo(numberOfErrors);
    }

    /***
     * Check that last processed is valid and it is between start and end time of request
     */
    @Then("^check that last processed date is valid$")
    public void checkLastProcessed() throws ParseException {
        refresh();
        String dateLabel = metricsTab.getLastProcessed();
        if (metricsTab.getNumberOfTotalMessages() == 0) {
            assertThat(dateLabel).matches("^No Data Available$");
            return;
        }
        assertThat(dateLabel).matches("^\\d{1,2}/\\d{1,2}/\\d{4}, \\d{1,2}:\\d{2}:\\d{2} (AM|PM)$");

        Date dateWhenProcessed = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a").parse(dateLabel);
        Calendar processedCalendar = Calendar.getInstance();
        processedCalendar.setTime(dateWhenProcessed);

        /*clear milliseconds because UI last processed label contains only minutes and seconds*/
        Calendar beforeRequest = calendarUtils.getBeforeRequest();
        beforeRequest.clear(Calendar.MILLISECOND);
        Calendar afterRequest = calendarUtils.getAfterRequest();
        afterRequest.clear(Calendar.MILLISECOND);
        // 5sec accuracy
        beforeRequest.add(Calendar.SECOND, -5);
        afterRequest.add(Calendar.SECOND, +5);
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!processedCalendar.equals(beforeRequest)) { //if not same then it tests whether it was after
            assertThat(processedCalendar.after(beforeRequest))
                .as("Check that processed date and time \"%s\" is after %s",
                    outFormat.format(processedCalendar.getTime()),
                    outFormat.format(beforeRequest.getTime()))
                .isTrue();
        }
        if (!processedCalendar.equals(afterRequest)) { //if not same then it tests whether it was before
            assertThat(processedCalendar.before(afterRequest))
                .as("Check that processed date and time \"%s\" is before %s",
                    outFormat.format(processedCalendar.getTime()),
                    outFormat.format(afterRequest.getTime()))
                .isTrue();
        }
    }

    @Then("^check that number of valid messages is (\\w+)$")
    public void checkNumberOfValidMessages(int numberOfValidMessages) {
        refresh();
        assertThat(metricsTab.getNumberOfValidMessages()).isEqualTo(numberOfValidMessages);
    }

    @Then("^check that number of error messages is (\\w+)$")
    public void checkNumberOfErrorMessages(int numberOfErrorMessages) {
        refresh();
        assertThat(metricsTab.getNumberOfErrorMessages()).isEqualTo(numberOfErrorMessages);
    }

    @Then("^check that number of total messages is (\\w+)$")
    public void checkNumberOfTotalMessages(int numberOfTotalMessages) {
        refresh();
        assertThat(metricsTab.getNumberOfTotalMessages()).isEqualTo(numberOfTotalMessages);
    }

    @Then("^check that uptime for ([^\"]*) pod is valid$")
    public void checkUptime(String integration) throws ParseException, InterruptedException {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(integration);
        if (pod.isPresent()) {
            refresh();
            // To minimize inaccuracy they should be called together
            String uptime = metricsTab.getUpTime();
            String openshiftTime = pod.get().getStatus().getStartTime();

            if (uptime.contains("No Data Available")) {
                TestUtils.sleepIgnoreInterrupt(61000); // gh-5100
                refresh();
                uptime = metricsTab.getUpTime();
                openshiftTime = pod.get().getStatus().getStartTime();
            }

            Pattern pattern = Pattern.compile("^(\\d{1,2}) (second|minute)(s)?$");
            Matcher matcher = pattern.matcher(uptime);
            if (!matcher.find()) {
                fail("UI uptime " + uptime + " doesn't match pattern.");
            }
            Long uiMinute = (matcher.group(2).contains("second")) ? 1 : Long.parseLong(matcher.group(1));

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date openShiftStartDate = format.parse(openshiftTime);
            long diff = new Date().getTime() - openShiftStartDate.getTime();
            long openShiftMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            assertThat(uiMinute)
                .as("Check that UI uptime '%s' is same as openshift uptime '%s'.",
                    uiMinute, openShiftMinutes)
                .isCloseTo(openShiftMinutes, within(Long.valueOf(1))); //1-second inaccuracy can happen
        } else {
            fail("Pod with name " + integration + " doesn't exist!");
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
                fail("UI time is same as actual time which is impossible");
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

            assertThat(uiDate.getTime())
                .as("Check that UI date of pod '%s' is same as date in the openshift '%s'.",
                    startTime, openshiftTime)
                .isCloseTo(openshiftDate.getTime(), 60000L); // UI sometimes round time UP sometimes not
        } else {
            fail("Pod with name " + integration + " doesn't exist!");
        }
    }

    /**
     * Parse since date to the Date
     * e.g. Since Dec 19th 10:42 -> group1 = Dec 19 group2= 10:42
     */
    private Date parseUiSinceDate(String dateLable) throws ParseException {
        Pattern pattern = Pattern.compile("^.* (\\d{1,2}/\\d{1,2}/\\d{4}, \\d{1,2}:\\d{2}:\\d{2} (AM|PM))$");
        Matcher matcher = pattern.matcher(dateLable);
        if (!matcher.find()) {
            fail("Date " + dateLable + " doesn't match pattern.");
        }
        // e.g. 6/13/2019, 2:09:47
        return new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a")
            .parse(matcher.group(1));
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
