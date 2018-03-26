package io.syndesis.qe.pages.customizations.connectors.wizard.steps;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.logic.common.wizard.WizardPhase;

public class ReviewActions extends SyndesisPageObject implements WizardPhase {

    private static class Button {
        public static By NEXT = By.xpath("//button[contains(.,'Next')]");
    }

    private static class Element {
        public static By ROOT = By.cssSelector("syndesis-api-connector-review");
    }

    private static class Input {
        public static By CONNECTOR_NAME = By.id("name");
        public static By HOST = By.id("host");
        public static By BASE_URL = By.id("basePath");
    }

    private static class TextArea {
        public static By DESCRIPTION = By.id("description");
    }

    @Override
    public void goToNextWizardPhase() {
        $(Button.NEXT).shouldBe(visible).click();
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }

    public void setConnectorName(String name) {
        $(Input.CONNECTOR_NAME).setValue(name);
    }
    public void setDescription(String description) {
        $(TextArea.DESCRIPTION).setValue(description);
    }

    public void setHost(String host) {
        $(Input.HOST).setValue(host);
    }

    public void setBaseUrl(String baseUrl) {
        $(Input.BASE_URL).setValue(baseUrl);
    }
}
