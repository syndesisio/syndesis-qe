package io.syndesis.qe.reports.selectors;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class SelectorUsageInfo {

    private static final Pattern URL_REPLACE_PATTERN = Pattern.compile("\\?\\w+=?[^/]*(\\/|$)");
    private static final Pattern URL_REPLACE_POD_ID = Pattern.compile("/-[^/]+/");
    private static final Pattern URL_REPLACE_INTEGRATION_ID = Pattern.compile("/i-[^/]+/");

    public final String selector;
    public String url;
    public final Set<String> scenarios;
    public final String stackTrace;
    public final String imgPath;
    public String dataTestId = null;

    public SelectorUsageInfo(String selector, String url, Set<String> scenarios, String stackTrace, String imgPath) {
        this.selector = selector;
        this.scenarios = scenarios;
        this.stackTrace = stackTrace;
        this.imgPath = imgPath;
        this.url = deparametrizeURL(url);
    }

    protected String deparametrizeURL(String str) {
        Matcher m = URL_REPLACE_PATTERN.matcher(str);
        Matcher m1 = URL_REPLACE_POD_ID.matcher(m.replaceAll("/"));
        Matcher m2 = URL_REPLACE_INTEGRATION_ID.matcher(m1.replaceAll("/-rAnd0mP0D1D/"));
        return m2.replaceAll("/i-rAndOm1nteGraTionID/");
    }

    public boolean hasDataTestId() {
        return dataTestId != null;
    }
}
