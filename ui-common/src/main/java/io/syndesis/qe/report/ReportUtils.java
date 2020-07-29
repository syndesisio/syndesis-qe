package io.syndesis.qe.report;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains various helper methods to use in templates
 */
public class ReportUtils {

    private static final Pattern GH_URL = Pattern.compile("(.*)\\.\\w+\\.[\\w$]+\\((\\w+.\\w+):(\\d+)\\)");

    public static String getGithubURL(String sTrace) {
        Matcher m = GH_URL.matcher(sTrace);
        if (m.matches()) {
            return String.format("%s/%s#L%s", m.group(1).replace('.', '/'), m.group(2), m.group(3));
        }
        return sTrace;
    }

    /**
     * Extracts just the feature path of a scenario
     */
    public static Set<String> shortenScenarios(Set<String> scenarios) {
        return scenarios.stream()
            .map(scenario -> scenario.split(":")[1])
            .map(filename -> filename.replace("features/", ""))
            .collect(Collectors.toSet());
    }
}
