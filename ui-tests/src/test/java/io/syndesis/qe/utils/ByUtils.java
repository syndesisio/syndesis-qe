package io.syndesis.qe.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class ByUtils {

    public static By dataTestId(String dataTestId) {
        return By.cssSelector(String.format("*[data-testid=\"%s\"]", dataTestId));
    }

    public static By dataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid=\"%s\"]", tag, dataTestId));
    }

    public static By containsDataTestId(String dataTestId) {
        return By.cssSelector(String.format("*[data-testid*=\"%s\"]", dataTestId));
    }

    public static By containsDataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid*=\"%s\"]", tag, dataTestId));
    }

    public static By dataTestIdContainsSubstring(String substring, int nthElement) {
        return By.xpath(String.format("(//*[contains(@data-testid,\"%s\")])[%d]", substring, nthElement));
    }

    public static By rowid(String rowId) {
        return By.cssSelector(String.format("*[rowid=\"%s\"]", rowId));
    }

    public static By partialLinkText(String partialText) {
        return new ByPartialText(partialText);
    }

    private static class ByPartialText extends By {

        private final String partialText;

        private ByPartialText(String partialText) {
            this.partialText = partialText;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.cssSelector("a,button")).stream()
                .filter(webElement -> webElement.getText().contains(partialText))
                .collect(Collectors.toList());
        }
    }
}
