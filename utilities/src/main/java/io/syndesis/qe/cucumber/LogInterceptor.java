package io.syndesis.qe.cucumber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Getter;

/**
 * This class stores all log events.
 */
public class LogInterceptor extends Filter<ILoggingEvent> {

    @Getter
    private static List<String> eventsStack = new ArrayList<>();
    private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    @Override
    public FilterReply decide(ILoggingEvent event) {
        eventsStack.add(String.format("[%s] %s - [%s:%d] %s", DATA_FORMAT.format(event.getTimeStamp()), event.getLevel(), event.getLoggerName(),
            event.getCallerData()[0].getLineNumber(), event.getFormattedMessage()));
        return FilterReply.ACCEPT;
    }
}
