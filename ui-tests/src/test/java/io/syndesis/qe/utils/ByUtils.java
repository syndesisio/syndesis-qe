package io.syndesis.qe.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class ByUtils {

    private ByUtils() {
        //NOOP
    }

    private static String extractSelectorFromBy(By by) {
        return by.toString().replace("By.cssSelector: ", "");
    }

    public static By dataTestId(String dataTestId) {
        return dataTestId("*", dataTestId);
    }

    public static By dataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid=\"%s\"]", tag, dataTestId));
    }

    public static By containsDataTestId(String dataTestId) {
        return containsDataTestId("*", dataTestId);
    }

    public static By containsDataTestId(String tag, String dataTestId) {
        return By.cssSelector(String.format("%s[data-testid*=\"%s\"]", tag, dataTestId));
    }

    public static By dataTestIdContainsSubstring(String substring, int nthElement) {
        return By.xpath(String.format("(//*[contains(@data-testid,\"%s\")])[%d]", substring, nthElement));
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

    /**
     * Tests for the presence of attribute, no matter the value
     * As an example you can imagine the {@code autoplay} attribute of a {@literal <video />}
     */
    public static By hasAttribute(String tag, String attrName) {
        return By.cssSelector(String.format("%s[%s]", tag, attrName));
    }

    /**
     * Tests for the presence of attribute, no matter the value
     * As an example you can imagine the {@code autoplay} attribute of a <video />
     */
    public static By hasAttribute(String attrName) {
        return hasAttribute("*", attrName);
    }

    /**
     * Attribute {@code attrName} is exact value of {@code val}
     */
    public static By attributeIsValue(String attrName, String val) {
        return attributeIsValue("*", attrName, val);
    }

    /**
     * Attribute {@code attrName} is exact value of {@code val}
     */
    public static By attributeIsValue(String tag, String attrName, String val) {
        return By.cssSelector(String.format("%s[%s=\"%s\"]", tag, attrName, val));
    }

    /**
     * Attribute {@code attrName} contains substring of {@code val}
     */
    public static By attributeContainsValue(String attrName, String val) {
        return attributeContainsValue("*", attrName, val);
    }

    /**
     * Attribute {@code attrName} contains substring of {@code val}
     */
    public static By attributeContainsValue(String tag, String attrName, String val) {
        return By.cssSelector(String.format("%s[%s*=\"%s\"]", tag, attrName, val));
    }

    public static By attributeInSpaceSeparatedList(String attrName, String val) {
        return attributeInSpaceSeparatedList("*", attrName, val);
    }

    public static By attributeInSpaceSeparatedList(String tag, String attrName, String val) {
        return By.cssSelector(String.format("%s[%s~=\"%s\"]", tag, attrName, val));
    }

    public static By attributeInCommaSeparatedList(String attrName, String val) {
        return attributeInCommaSeparatedList("*", attrName, val);
    }

    public static By attributeInCommaSeparatedList(String tag, String attrName, String val) {
        return By.cssSelector(String.format("%s[%s~=\"%s\"]", tag, attrName, val));
    }

    /**
     * Selects only direct children of parent element
     * For directChild(div, img) this applies:
     * {@literal
     * <div>
     * <img> <-- is selected
     * <nav>
     * <img> <-- isn't selected
     * </nav>
     * </div>
     * }
     */
    public static By directChild(By parent, By child) {
        return directChild(extractSelectorFromBy(parent), extractSelectorFromBy(child));
    }

    /**
     * Selects only direct children of parent element
     * For directChild(div, img) this applies:
     * {@literal
     * <div>
     * <img> <-- is selected
     * <nav>
     * <img> <-- isn't selected
     * </nav>
     * </div>
     * }
     */
    public static By directChild(String parent, String child) {
        return By.cssSelector(String.format("%s > %s", parent, child));
    }

    /**
     * Selects the first sibling that is right next to siblingSelector
     * for adjacentSibling(div, img)
     * {@literal
     * <div>
     * <img> <-- not selected
     * <div></div>
     * <img> <-- selected
     * <img> <-- not selected
     *
     * </div>
     * }
     */
    public static By adjacentSibling(By siblingSelector, By selectedSibling) {
        return adjacentSibling(extractSelectorFromBy(siblingSelector), extractSelectorFromBy(selectedSibling));
    }

    /**
     * Selects the first sibling that is right next to siblingSelector
     * for adjacentSibling(div, img)
     * {@literal
     * <div>
     * <img> <-- not selected
     * <div></div>
     * <img> <-- selected
     * <img> <-- not selected
     *
     * </div>
     * }
     */
    public static By adjacentSibling(String siblingSelector, String selectedSibling) {
        return By.cssSelector(String.format("%s + %s", siblingSelector, selectedSibling));
    }

    /**
     * Selects any sibling that is after siblingSelector
     * for adjacentSibling(div, img)
     * {@literal
     * <div>
     * <img> <-- not selected
     * <div></div>
     * <img> <-- selected
     * <img> <-- selected
     *
     * </div>
     * }
     */
    public static By generalSibling(By siblingSelector, By selectedSibling) {
        return generalSibling(extractSelectorFromBy(siblingSelector), extractSelectorFromBy(selectedSibling));
    }

    /**
     * Selects any sibling that is after siblingSelector
     * for adjacentSibling(div, img)
     * {@literal
     * <div>
     * <img> <-- not selected
     * <div></div>
     * <img> <-- selected
     * <img> <-- selected
     *
     * </div>}
     */
    public static By generalSibling(String siblingSelector, String selectedSibling) {
        return By.cssSelector(String.format("%s ~ %s", siblingSelector, selectedSibling));
    }

    public static ByBuilder builder() {
        return new ByBuilder();
    }
}
