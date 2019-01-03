package io.syndesis.qe.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * This class is only for saving time before and after request. After that, the time in the any step can be compare
 * whether it is between these two date.
 */
@Slf4j
@Component
public class CalendarUtils {

    @Getter
    @Setter
    private Calendar beforeRequest = null;

    @Getter
    @Setter
    private Calendar afterRequest = null;

}
