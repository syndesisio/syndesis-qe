package io.syndesis.qe.pages.dashboard;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class DashboardPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-dashboard");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public SelenideElement getConnection(String connectionTitle) {
        log.info("searching for connection {}", connectionTitle);
        return this.getRootElement().$(String.format("h2.card-pf-title.text-center[title=\"%s\"]", connectionTitle));
    }

    public void goToConnection(String connectionTitle) {
        log.info("searching for connection {}", connectionTitle);
        this.getConnection(connectionTitle).shouldBe(visible).click();
    }

    public boolean isIntegrationPresent(String integrationName) {
        log.info("Checking if integration {} is present in the list", integrationName);
        SelenideElement integration = this.getRootElement().find(By.cssSelector(String.format("div[innertext='%s']", integrationName)));
        return integration.is(visible);
    }

}
