package io.syndesis.qe.pages.integrations.summary;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metrics extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("integration-detail-metrics");

        public static final By ROOT_TOTAL_ERROR = ByUtils.dataTestId("integration-detail-metrics-total-errors-card");
        public static final By TOTAL_ERROR = ByUtils.dataTestId("integration-detail-metrics-total-errors");

        public static final By ROOT_LAST_PROCESSED = ByUtils.dataTestId("integration-detail-metrics-last-processed-card");
        public static final By LAST_PROCESSED = ByUtils.dataTestId("integration-detail-metrics-last-processed");

        public static final By ROOT_TOTAL_MESSAGES = By.className("aggregate-status");
        public static final By TOTAL_COUNT = ByUtils.dataTestId("aggregated-metric-card-total-count");
        public static final By OK_COUNT = ByUtils.dataTestId("aggregated-metric-card-ok-count");
        public static final By ERROR_COUNT = ByUtils.dataTestId("aggregated-metric-card-error-count");

        public static final By ROOT_UPTIME = ByUtils.dataTestId("dashboard-page-metrics-uptime");
        public static final By UPTIME_SINCE = ByUtils.dataTestId("dashboard-page-metrics-uptime-since");
        public static final By UPTIME_DURATION = ByUtils.dataTestId("dashboard-page-metrics-uptime-duration");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public int getTotalErrors() {
        SelenideElement numberOfErrors = getRootElement().find(Element.ROOT_TOTAL_ERROR).shouldBe(visible)
            .find(Element.TOTAL_ERROR);
        return Integer.parseInt(numberOfErrors.getText().trim());
    }

    public String getLastProcessed() {
        return getRootElement().find(Element.ROOT_LAST_PROCESSED).shouldBe(visible)
            .find(Element.LAST_PROCESSED).getText();
    }

    public int getNumberOfValidMessages() {
        SelenideElement numberOfValidMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(Element.OK_COUNT);
        return Integer.parseInt(numberOfValidMessages.getText().trim());
    }

    public int getNumberOfErrorMessages() {
        SelenideElement numberOfMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(Element.ERROR_COUNT);
        return Integer.parseInt(numberOfMessages.getText());
    }

    public int getNumberOfTotalMessages() {
        SelenideElement numberOfTotalMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(Element.TOTAL_COUNT);
        return Integer.parseInt(numberOfTotalMessages.getText().trim());
    }

    public String getUpTime() {
        return getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .find(Element.UPTIME_DURATION).getText();
    }

    public String getStartTime() {
        return getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .find(Element.UPTIME_SINCE).getText();
    }
}
