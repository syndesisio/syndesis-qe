package io.syndesis.qe.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class ByUtils {

    public static By dataTestId(String dataTestId) {
        return By.cssSelector(String.format("*[data-testid~=\"%s\"]", dataTestId));
    }

    public static By dataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid~=\"%s\"]", tag, dataTestId));
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
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext searchContext) {
                return searchContext.findElements(By.cssSelector("a,button,input[type=\"button\"][type=\"submit\"")).stream()
                    .filter(webElement -> webElement.getText().contains(partialText))
                    .collect(Collectors.toList());
            }
        };
    }

    public static By containsId(String id) {
        return By.cssSelector(String.format("*[id*=\"%s\"]", id));
    }

    public static By containsClassName(String id) {
        return By.cssSelector(String.format("*[class*=\"%s\"]", id));
    }

    public static By containsLabel(String label) {
        return By.cssSelector(String.format("*[label*=\"%s\"]", label));
    }

    /**
     * By custom attribute (e.g. for attributes which are used only once and it doesn't worth adding here as a separate method)
     */
    public static By customAttribute(String attribute, String value) {
        return By.cssSelector(String.format("*[%s~=\"%s\"]", attribute, value));
    }

    public static By hasEqualText(String elementForSearch, String value) {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext searchContext) {
                return searchContext.findElements(By.cssSelector(elementForSearch)).stream()
                    .filter(webElement -> webElement.getText().equals(value))
                    .collect(Collectors.toList());
            }
        };
    }
}
