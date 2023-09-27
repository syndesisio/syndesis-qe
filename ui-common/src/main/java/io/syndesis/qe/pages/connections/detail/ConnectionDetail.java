package io.syndesis.qe.pages.connections.detail;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionDetail extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("pf-c-page__main");
        public static final By CONNECTION_DETAIL = By.className("pf-c-content");
        public static final By CONNECTION_NAME = By.cssSelector(".inline-text-readwidget.connection-details-header__connectionName");
        public static final By CONNECTION_HEADER = By.cssSelector(".connection-details-header__row");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public SelenideElement connectionDetailElement() {
        return $(Element.CONNECTION_DETAIL);
    }

    public String connectionName() {
        return $(Element.CONNECTION_HEADER).$(Element.CONNECTION_NAME).shouldBe(visible).getText();
    }

    public String getDescription() {
        return this.connectionDetailElement().find(By.className("inline-text-readwidget")).getText();
    }

    public void setDescription(String description) {
        this.connectionDetailElement().find(By.className("inline-text-readwidget")).click();
        this.connectionDetailElement().find(By.id("inline-edit-textarea")).clear();
        this.connectionDetailElement().find(By.id("inline-edit-textarea")).setValue(description);
        //confirm and cancel buttons are indentical:
        this.connectionDetailElement().findAll(By.className("pf-c-button")).get(0).click();
        TestUtils.sleepForJenkinsDelayIfHigher(5);
    }
}
