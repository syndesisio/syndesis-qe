package io.syndesis.qe.pages;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class SupportPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.tagName("syndesis-support");

        public static final By SPECIFIC_LOGS = By.id("specificlogs");
        public static final By ALL_LOGS = By.id("alllogs");

        public static final By CHECKBOX = By.xpath("//*[@type='checkbox']");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void selectAllIntegrationDownload() {
        $(Element.ALL_LOGS).shouldBe(visible).click();
    }

    public void selectSpecificIntegrationDownload(String integrationName) throws InterruptedException {
        $(Element.SPECIFIC_LOGS).shouldBe(visible).click();
        $(Element.CHECKBOX).shouldBe(visible).click();
    }

    public File downloadZipLogs() throws InterruptedException {
        getButton("Download").shouldBe(visible).click();

        Assertions.assertThat(TestUtils.waitForEvent(File::exists,
                () -> new File(CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "syndesis.zip"),
                TimeUnit.MINUTES, 5, TimeUnit.SECONDS, 5))
                .isTrue();

        return new File(CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "syndesis.zip");
    }

    public String getVersion() {
        //TODO: lets wait here until they add ID for version string in the product
        return "";
    }
}
