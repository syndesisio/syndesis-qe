package io.syndesis.qe.utils;

import org.openqa.selenium.By;

public class ByUtils {

    public static By dataTestId(String dataTestId) {
        return By.cssSelector(String.format("*[data-testid=\"%s\"]", dataTestId));
    }

    public static By dataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid=\"%s\"]", tag, dataTestId));
    }
}
