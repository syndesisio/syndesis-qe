package io.syndesis.qe.fragments.common.form;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.UploadFile;

import org.openqa.selenium.By;

import java.io.File;

/**
 * General implementation for the component used in API connector and API provider
 */
public class ApiSpecificationForm {
    private static class Input {
        public static By CHOOSE_FILE = By.className("open-api-select-method__dnd-container");
        public static By URL = By.id("method");
        public static By UPLOAD_AN_OPENAPI_FILE = By.id("method-file");
        public static By USE_A_URL = By.id("method-url");
        public static By CREATE_FROM_SCRATCH = By.xpath("//input[@type='radio' and ../text()[contains(.,'Create from scratch')]]");
    }

    public void upload(String source, String url) {
        switch (source) {
            case "file":
                uploadFileFromPath(url);
                break;
            case "url":
                uploadFileFromUrl(url);
                break;
            case "scratch":
                createFromScratch();
                break;
            default:
                break;
        }
    }

    public void uploadFileFromPath(String path) {
        $(Input.UPLOAD_AN_OPENAPI_FILE).shouldBe(visible).click();
        UploadFile.uploadFile($(Input.CHOOSE_FILE).shouldBe(visible).$(By.tagName("input")),
            new File(getClass().getClassLoader().getResource(path).getFile()));
    }

    public void uploadFileFromUrl(String url) {
        $(Input.USE_A_URL).shouldBe(visible).click();
        $(Input.URL).shouldBe(visible).sendKeys(url);
    }

    public void createFromScratch() {
        $(Input.CREATE_FROM_SCRATCH).shouldBe(visible).setSelected(true);
    }
}
