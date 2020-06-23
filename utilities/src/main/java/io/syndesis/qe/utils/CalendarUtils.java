package io.syndesis.qe.utils;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Calendar;

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
        this.lastBeforeRequest = beforeRequest;
    }

    public void setAfterRequest(Calendar afterRequest, String partialPodName) {
        this.lastAfterRequest = afterRequest;
    }
}
