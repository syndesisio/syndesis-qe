package io.syndesis.qe.utils;

import io.syndesis.qe.TestConfiguration;

import org.springframework.stereotype.Component;

@Component
public class ServiceNowUtils {
    private static final String BROWSER = TestConfiguration.syndesisBrowser();

    public String modifySNNumber(String input) {
        if (input.contains("{number1}")) {
            return input.replace("{number1}", "QA" + BROWSER.substring(0, 4).toUpperCase() + "1");
        } else if (input.contains("{number2}")) {
            return input.replace("{number2}", "QACR" + BROWSER.substring(0, 2).toUpperCase() + "1");
        } else {
            // add browser info to the other input
            return input + BROWSER.substring(0, 2).toUpperCase() + "1";
        }
    }
}
