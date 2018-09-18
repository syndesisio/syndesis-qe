package io.syndesis.qe.pages.customizations.connectors.wizard.steps;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.io.File;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class UploadSwaggerSpecification extends SyndesisPageObject implements WizardPhase {

    private static class Button {
        public static By NEXT = By.xpath("//button[contains(.,'Next')]");
    }

    private static class Element {
        public static By ROOT = By.cssSelector("syndesis-api-connector-swagger-upload");
    }

    private static class Input {
        public static By CHOOSE_FILE = By.xpath("//input[@type='file']");
        public static By URL = By.name("swaggerFileUrl");
        public static By UPLOAD_AN_OPENAPI_FILE = By.xpath("//input[@type='radio' and ../text()[contains(.,'Upload an OpenAPI file')]]");
        public static By USE_A_URL = By.xpath("//input[@type='radio' and ../text()[contains(.,'Use a URL')]]");
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

    public void upload(String source, String url) {
        switch (source) {
            case "file":
                uploadFileFromPath(url);
                break;
            case "url":
                uploadFileFromUrl(url);
                break;
            default:
                break;
        }
    }

    public void uploadFileFromPath(String path) {
        $(Input.UPLOAD_AN_OPENAPI_FILE).shouldBe(visible).setSelected(true);
        $(Input.CHOOSE_FILE).shouldBe(visible).uploadFile(new File(getClass().getClassLoader().getResource(path).getFile()));
    }

    public void uploadFileFromUrl(String url) {
        $(Input.USE_A_URL).shouldBe(visible).setSelected(true);
        $(Input.URL).shouldBe(visible).sendKeys(url);
    }
}
