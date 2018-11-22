package io.syndesis.qe.fragments.common.form;

import java.io.File;

import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

/**
 * General implementation for the component used in API connector and API provider
 *
 */
public class ApiSpecificationForm {
    private static class Input {
        public static By CHOOSE_FILE = By.xpath("//input[@type='file']");
        public static By URL = By.name("swaggerFileUrl");
        public static By UPLOAD_AN_OPENAPI_FILE = By.xpath("//input[@type='radio' and ../text()[contains(.,'Upload an OpenAPI file')]]");
        public static By USE_A_URL = By.xpath("//input[@type='radio' and ../text()[contains(.,'Use a URL')]]");
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
        $(Input.UPLOAD_AN_OPENAPI_FILE).shouldBe(visible).setSelected(true);
        $(Input.CHOOSE_FILE).shouldBe(visible).uploadFile(new File(getClass().getClassLoader().getResource(path).getFile()));
    }

    public void uploadFileFromUrl(String url) {
        $(Input.USE_A_URL).shouldBe(visible).setSelected(true);
        $(Input.URL).shouldBe(visible).sendKeys(url);
    }

    public void createFromScratch() {
        $(Input.CREATE_FROM_SCRATCH).shouldBe(visible).setSelected(true);
    }
}
