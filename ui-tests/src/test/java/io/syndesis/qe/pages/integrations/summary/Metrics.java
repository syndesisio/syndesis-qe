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
        public static final By ROOT_LAST_POCESSED = ByUtils.dataTestId("integration-detail-metrics-last-processed-card");
        public static final By ROOT_TOTAL_MESSAGES = By.className("aggregate-status");
        //div has two h3 elements but contains method wants only one as first argument so the class have to be specified
        public static final By ROOT_UPTIME = ByUtils.dataTestId("dashboard-page-metrics-uptime");
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
            .find(By.className("pf-c-card__body"));
        return Integer.parseInt(numberOfErrors.getText().trim());
    }

    public String getLastProcessed() {
        SelenideElement lastProcessed = getRootElement().find(Element.ROOT_LAST_POCESSED).shouldBe(visible)
            .find(By.className("pf-c-card__body"));
        return lastProcessed.getText();
    }

    public int getNumberOfValidMessages() {
        SelenideElement numberOfValidMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(ByUtils.dataTestId("aggregated-metric-card-ok-count"));
        return Integer.parseInt(numberOfValidMessages.getText().trim());
    }

    public int getNumberOfErrorMessages() {
        SelenideElement numberOfMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(ByUtils.dataTestId("aggregated-metric-card-error-count"));
        return Integer.parseInt(numberOfMessages.getText());
    }

    public int getNumberOfTotalMessages() {
        SelenideElement numberOfTotalMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(ByUtils.dataTestId("aggregated-metric-card-total-count"));
        return Integer.parseInt(numberOfTotalMessages.getText().trim());
    }

    public String getUpTime() {
        SelenideElement uptime = getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .findAll(By.className("pf-c-card__body")).get(1);
        return uptime.getText();
    }

    public String getStartTime() {
        SelenideElement date = getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .find(By.className("metrics-uptime__uptime"));
        return date.getText();
    }
}
