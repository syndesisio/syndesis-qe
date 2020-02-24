package io.syndesis.qe.utils;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is only for saving time before and after request. After that, the time in the any step can be compare
 * whether it is between these two date.
 */
@Slf4j
@Component
@Lazy
public class CalendarUtils {

    /*-2 means that OCP is 2 seconds behind of the instance where the test is running
      +2 means that OCP is 2 seconds ahead of the instance where the test is running */
    private int diffSeconds = 0;
    private int diffMinutes = 0;
    private int diffHours = 0;

    public CalendarUtils() throws ParseException {
        String ocpTime = OpenShiftUtils.binary().execute(
            "run", "--serviceaccount", "builder", "--image", "okansahiner/kube-diag",
            "kube-diag", "-it", "--restart=Never", "--attach", "--rm", "--command", "--", "bash", "-c", "\"date\"");

        Date ocpDate = new SimpleDateFormat("EEE MMM d hh:mm:ss zzz yyyy").parse(ocpTime);
        Date testDate = new Date();
        int diff = Math.toIntExact(ocpDate.getTime() - testDate.getTime());
        diffSeconds = diff / 1000 % 60;
        diffMinutes = diff / (60 * 1000) % 60;
        diffHours = diff / (60 * 60 * 1000);
    }

    @Getter
    private Calendar beforeRequest = null;

    @Getter
    private Calendar afterRequest = null;

    public void setBeforeRequest(Calendar beforeRequest) {
        syncTimeWithOcpInstance(beforeRequest);
        this.beforeRequest = beforeRequest;
    }

    public void setAfterRequest(Calendar afterRequest) {
        syncTimeWithOcpInstance(afterRequest);
        this.afterRequest = afterRequest;
    }

    //correct time, sync with the OCP time
    private void syncTimeWithOcpInstance(Calendar calendar) {
        log.info("Time before sync: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        log.info(String.format("Sync info: HOUR:'%s' MINUTE:'%s' SECOND:'%s'", diffHours, diffMinutes, diffSeconds));
        calendar.add(Calendar.SECOND, diffSeconds);
        calendar.add(Calendar.MINUTE, diffMinutes);
        calendar.add(Calendar.HOUR, diffHours);
        log.info("Time after sync: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
    }
}
