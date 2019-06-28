package io.syndesis.qe.pages.integrations.editor.apiprovider.wizard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

public class ReviewApiProviderActions extends SyndesisPageObject implements WizardPhase {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".open-api-review-actions");
        public static final By OPERATIONS = By.cssSelector("div.openapi-review-actions .review-actions__heading ~ p");
        public static final By TAGGED_OPERATIONS = By.cssSelector("div.openapi-review-actions" +
                " ol.openapi-review-actions__operations ol.openapi-review-actions__operations");
        public static final By WARNINGS = By.cssSelector(".openapi-review-actions__label--warning");
        public static final By ERRORS = By.cssSelector(".openapi-review-actions__label--error");
    }

    private static class Button {
        public static By NEXT = By.xpath("//button[contains(.,'Next')]");
    }

    private static final Pattern TAGGED_PATTERN = Pattern.compile("(\\d+) tagged (\\w+)");

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    @Override
    public void goToNextWizardPhase() {
        $(Button.NEXT).shouldBe(visible).click();
    }

    public int getNumberOfOperations() {
        String operations = $(Element.OPERATIONS).$("li strong").shouldBe(visible).getText().trim();
        return Integer.parseInt(operations);
    }

    public int getNumberOfOperationsByTag(String tag) {
        ElementsCollection operations = $(Element.TAGGED_OPERATIONS).$$("li");
        for (SelenideElement e : operations) {
            Matcher m = TAGGED_PATTERN.matcher(e.getText());
            if (m.find() && tag.equals(m.group(2))) {
                return Integer.parseInt(m.group(1));
            }
        }
        return 0;
    }

    public int getNumberOfWarnings() {
        return getNumberFromLabel(Element.WARNINGS);
    }

    public int getNumberOfErrors() {
        return getNumberFromLabel(Element.ERRORS);
    }

    private int getNumberFromLabel(By selector) {
        SelenideElement labelElement = $(selector);
        if (labelElement.exists()) {
            return Integer.parseInt(labelElement.getText());
        } else {
            return 0;
        }
    }
}
