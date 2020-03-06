package io.syndesis.qe.utils;

import static org.assertj.core.api.Java6Assertions.fail;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.Pod;
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

    @Getter
    private Calendar lastBeforeRequest = null;

    @Getter
    private Calendar lastAfterRequest = null;

    public void setBeforeRequest(Calendar beforeRequest, String partialPodName) {
        syncTimeWithOcpInstance(beforeRequest, partialPodName);
        this.lastBeforeRequest = beforeRequest;
    }

    public void setAfterRequest(Calendar afterRequest, String partialPodName) {
        syncTimeWithOcpInstance(afterRequest, partialPodName);
        this.lastAfterRequest = afterRequest;
    }

    //correct time, sync with the OCP time
    private void syncTimeWithOcpInstance(Calendar calendar, String partialPodName) {
        Optional<Pod> podByPartialName = OpenShiftUtils.getPodByPartialName(partialPodName);
        if (!podByPartialName.isPresent()) {
            fail("Integration with the partial pod name '" + partialPodName + "' does not exist.");
        }
        String integrationTime = OpenShiftUtils.binary().execute(
            "exec", podByPartialName.get().getMetadata().getName(), "date");
        Date testDate = new Date();
        Date ocpDate = null;
        log.info("Time in the integration pod is: " + integrationTime);
        log.info("Time in test suite is: " + testDate);
        try {
            ocpDate = new SimpleDateFormat("EEE MMM d hh:mm:ss zzz yyyy").parse(integrationTime);
        } catch (ParseException e) {
            fail("Exception during parsing the date from the integration pod", e);
        }
        int diff = Math.toIntExact(ocpDate.getTime() - testDate.getTime());

        int diffSeconds = diff / 1000 % 60;
        int diffMinutes = diff / (60 * 1000) % 60;
        int diffHours = diff / (60 * 60 * 1000);

        log.info(String.format("Sync info: HOUR:'%s' MINUTE:'%s' SECOND:'%s'", diffHours, diffMinutes, diffSeconds));
        calendar.add(Calendar.SECOND, diffSeconds);
        calendar.add(Calendar.MINUTE, diffMinutes);
        calendar.add(Calendar.HOUR, diffHours);
        log.info("Test suite time after sync: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
    }
}
