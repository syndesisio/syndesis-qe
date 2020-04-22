package io.syndesis.qe.pages;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupportPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.id("root");

        public static final By SPECIFIC_LOGS = ByUtils.dataTestId("support-page-specific-logs-input");
        public static final By ALL_LOGS = ByUtils.dataTestId("support-page-all-logs-input");
//        public static final By CHECKBOX = ByUtils.dataTestId("selective-integration-list-integrations-input");
        public static final By CHECKBOX = ByUtils.rowid("data-list-item-support-page-integration");
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

    public void selectSpecificIntegrationDownload(String integrationName) {
        $(Element.SPECIFIC_LOGS).shouldBe(visible).click();
        $(Element.CHECKBOX).shouldBe(visible).click();
    }

    public File downloadZipLogs() throws InterruptedException {
        getButton("Download").shouldBe(visible).click();

        assertThat(TestUtils.waitForEvent(File::exists,
            () -> new File(CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "syndesis.zip"),
            TimeUnit.MINUTES, 5, TimeUnit.SECONDS, 5))
            .isTrue();

        return new File(CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + "syndesis.zip");
    }

    public String getVersion() {
        TestUtils.waitFor(() -> $(By.id("pf-about-modal-content-1")).$$(By.tagName("dd")).size() == 3,
            3, 30, "Syndesis version was not loaded in 30s");

        List<SelenideElement> elementsWithSyndesisText = $(By.id("pf-about-modal-content-1")).$$(By.tagName("dd"));
        assertThat(elementsWithSyndesisText).hasSize(3);
        return elementsWithSyndesisText.get(0).getText();
    }
}
