package io.syndesis.qe.pages.customizations.connectors.wizard.steps;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ReviewEditConnectorDetails extends SyndesisPageObject implements WizardPhase {

    private static class Button {

        public static By CREATE_CONNECTOR = By.xpath(".//button[contains(.,'Create API Connector')]");
    }

    private static class Element {
        public static By ROOT = By.cssSelector("syndesis-api-connector-info");
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
        finish();
    }

    public void finish() {
        $(Button.CREATE_CONNECTOR).shouldBe(visible).click();
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
        $(Input.CONNECTOR_NAME).shouldBe(visible).setValue(name);
    }

    public void setDescription(String description) {
        $(TextArea.DESCRIPTION).shouldBe(visible).setValue(description);
    }

    public void setHost(String host) {
        $(Input.HOST).shouldBe(visible).setValue(host);
    }

    public void setBaseUrl(String baseUrl) {
        $(Input.BASE_URL).shouldBe(visible).setValue(baseUrl);
    }
}
