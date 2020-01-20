package io.syndesis.qe.fragments.common.form;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.UploadFile;

import org.junit.Assert;

import org.openqa.selenium.By;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * General implementation for the component used in API connector and API provider
 */
@Slf4j
public class ApiSpecificationForm {
    private static class Input {
        public static By CHOOSE_FILE = By.className("open-api-select-method__dnd-container");
        public static By URL = By.id("method");
        public static By UPLOAD_AN_OPENAPI_FILE = By.id("method-file");
        public static By USE_A_URL = By.id("method-url");
        public static By CREATE_FROM_SCRATCH_2X = By.id("method-scratch-2x");
        public static By CREATE_FROM_SCRATCH_3X = By.id("method-scratch-3x");
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
                createFromScratch(url);
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

    public void createFromScratch(String version) {
        if (version != null) {
            switch (version.toLowerCase()) {
                case "v2":
                    $(Input.CREATE_FROM_SCRATCH_2X).shouldBe(visible).click();
                    break;
                case "v3":
                    $(Input.CREATE_FROM_SCRATCH_3X).shouldBe(visible).click();
                    break;
                default:
                    log.error("Unknown API definition type {}", version);
                    Assert.fail("Unknown API definition type " + version);
            }
            return;
        }
        $(Input.CREATE_FROM_SCRATCH_2X).shouldBe(visible).click();
    }
}
