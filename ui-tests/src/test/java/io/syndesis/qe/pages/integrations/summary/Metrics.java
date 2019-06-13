package io.syndesis.qe.pages.integrations.summary;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metrics extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("integration-detail-metrics");
        public static final By ROOT_TOTAL_ERROR =
            By.xpath("//div[@class='card-pf card-pf-accented card-pf-aggregate-status card-pf-match-height' and contains(.,'Total Errors')]");
        public static final By ROOT_LAST_POCESSED =
            By.xpath("//div[@class='card-pf card-pf-accented card-pf-aggregate-status card-pf-match-height' and contains(.,'Last Processed')]");
        public static final By ROOT_TOTAL_MESSAGES =
            By.xpath("//div[@class='card-pf card-pf-accented card-pf-aggregate-status card-pf-match-height' and contains(.,'Total Messages')]");
        //div has two h3 elements but contains method wants only one as first argument so the class have to be specified
        public static final By ROOT_UPTIME =
            By.xpath("//div[@class='card-pf card-pf-accented card-pf-aggregate-status card-pf-match-height' and contains(.,'Uptime')]");
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
            .find(By.className("card-pf-aggregate-status-notification"));
        return Integer.parseInt(numberOfErrors.getText());
    }

    public String getLastProcessed() {
        SelenideElement lastProcessed = getRootElement().find(Element.ROOT_LAST_POCESSED).shouldBe(visible)
            .find(By.className("card-pf-aggregate-status-notification"));
        return lastProcessed.getText();
    }

    public int getNumberOfValidMessages() {
        SelenideElement numberOfValidMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(By.xpath(".//span[@class='card-pf-aggregate-status-notification' and span[@class='pficon pficon-ok']]"));
        return Integer.parseInt(numberOfValidMessages.getText().replaceAll("\\s+", ""));
    }

    public int getNumberOfErrorMessages() {
        SelenideElement numberOfMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(By.xpath(".//span[@class='card-pf-aggregate-status-notification' and span[@class='pficon pficon-error-circle-o']]"));
        return Integer.parseInt(numberOfMessages.getText());
    }

    public int getNumberOfTotalMessages() {
        SelenideElement numberOfTotalMessages = getRootElement().find(Element.ROOT_TOTAL_MESSAGES).shouldBe(visible)
            .find(By.className("card-pf-aggregate-status-count"));
        return Integer.parseInt(numberOfTotalMessages.getText().replaceAll("\\s+", ""));
    }

    public String getUpTime() {
        SelenideElement uptime = getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .find(By.className("card-pf-aggregate-status-notification"));
        return uptime.getText();
    }

    public String getStartTime() {
        SelenideElement date = getRootElement().find(Element.ROOT_UPTIME).shouldBe(visible)
            .find(By.className("integration-detail-metrics__uptime-uptime"));
        return date.getText();
    }
}
