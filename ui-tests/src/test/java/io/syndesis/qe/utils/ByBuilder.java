package io.syndesis.qe.utils;

import org.openqa.selenium.By;

public class ByBuilder {
    private StringBuilder selectors;

    public ByBuilder() {
        selectors = new StringBuilder();
    }

    public ByBuilder cssSelector(String selector) {
        selectors.append(selector);
        return this;
    }

    public ByBuilder id(String id) {
        selectors.append("#" + id);
        return this;
    }

    public ByBuilder name(String name) {
        selectors.append(String.format("[name=\"%s\"]", name));
        return this;
    }

    public ByBuilder tagName(String tagName) {
        selectors.append(tagName);
        return this;
    }

    public ByBuilder className(String className) {
        selectors.append("." + className);
        return this;
    }

    /**
     * {@code data-testid} is exact value of {@code value}
     */
    public ByBuilder dataTestId(String value) {
        selectors.append(String.format("[data-testid=\"%s\"]", value));
        return this;
    }

    /**
     * {@code data-testid} contains substring of {@code value}
     */
    public ByBuilder containsDataTestId(String value) {
        selectors.append(String.format("[data-testid*=\"%s\"]", value));
        return this;
    }

    /**
     * Attribute {@code attrName} contains substring of {@code value}
     */
    public ByBuilder attributeContainsValue(String attrName, String value) {
        selectors.append(String.format("[%s*=\"%s\"]", attrName, value));
        return this;
    }

    /**
     * Selects only direct children of parent element
     * For directChild(div, img) this applies:
     * <div>
     * <img> <-- is selected
     * <nav>
     * <img> <-- isn't selected
     * </nav>
     * </div>
     */
    public ByBuilder directChild(String child) {
        selectors.append(" > ");
        return this;
    }

    /**
     * Selects any children of parent element
     * For anyChild(div, img) this applies:
     * <div>
     * <img> <-- is selected
     * <nav>
     * <img> <-- is selected
     * </nav>
     * </div>
     */
    public ByBuilder anyChild(String child) {
        selectors.append(" ");
        return this;
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
    public ByBuilder adjacentSibling(String sibling) {
        selectors.append(" + ");
        return this;
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
    public ByBuilder generalSibling(String sibling) {
        selectors.append(" ~ ");
        return this;
    }

    public By build() {
        return By.cssSelector(selectors.toString());
    }
}
