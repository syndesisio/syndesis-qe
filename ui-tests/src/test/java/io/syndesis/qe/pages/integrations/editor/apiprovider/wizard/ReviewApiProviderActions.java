package io.syndesis.qe.pages.integrations.editor.apiprovider.wizard;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewApiProviderActions extends SyndesisPageObject implements WizardPhase {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".open-api-review-actions");
        public static final By OPERATIONS = By.xpath(".//*[text()=\"IMPORTED\"]/following-sibling::*");
        public static final By TAGGED_OPERATIONS = By.cssSelector("div.openapi-review-actions" +
                " ol.openapi-review-actions__operations ol.openapi-review-actions__operations");
        public static final By WARNINGS = By.xpath(".//*[text()=\"WARNINGS\"]");
        public static final By ERRORS = By.xpath(".//*[text()=\"ERRORS\"]");
    }

    private static class Button {
        public static By NEXT = By.xpath(".//button[contains(.,'Next')]");
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
        TestUtils.waitFor(() -> $(Element.OPERATIONS).is(visible), 1, 10, "No provider operations found");
        String operations = $(Element.OPERATIONS).shouldBe(visible).getText().trim().split(" ")[0];
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
            return Integer.parseInt(labelElement.$(By.className("label")).waitUntil(visible, 15000).getText());
        } else {
            return 0;
        }
    }
}
