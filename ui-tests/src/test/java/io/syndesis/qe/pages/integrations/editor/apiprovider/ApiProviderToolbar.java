package io.syndesis.qe.pages.integrations.editor.apiprovider;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiProviderToolbar extends SyndesisPageObject {
    private static final class Element {
        public static final By ROOT = By.cssSelector(".pf-c-page__main-breadcrumb");
        public static final By OPERATIONS_DROPDOWN = By.cssSelector(".operations-dropdown");
        public static final By GO_TO_OPERATION_LIST_BUTTON = By.xpath("//button[normalize-space()='Go to Operation List']");
        public static final By EDIT_OPENAPI_DEFINITION = By.xpath("//a[normalize-space()='View/Edit API Definition']");
    }

    private static final class Button {
        public static final By PUBLISH = By.xpath("//button[text()[contains(.,'Publish')]]");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void goToOperation(String operationName) {
        getRootElement().$(Element.OPERATIONS_DROPDOWN).click();
        getRootElement().$(Element.OPERATIONS_DROPDOWN)
            .$$(By.cssSelector("a strong"))
            .filter(Condition.text(operationName))
            .shouldHaveSize(1)
            .get(0)
            .click();
    }

    public void goToOperationList() {
        getRootElement().$(Element.OPERATIONS_DROPDOWN).click();
        if (!$(Element.GO_TO_OPERATION_LIST_BUTTON).exists()) {
            $$(By.className("pf-c-breadcrumb__item")).get(1).click();
        } else {
            getRootElement().$(Element.GO_TO_OPERATION_LIST_BUTTON).shouldBe(visible).click();
        }
    }

    public void editOpenApi() {
        getRootElement().$(Element.EDIT_OPENAPI_DEFINITION).shouldBe(visible).click();
    }

    public void publish() {
        this.getRootElement().$(Button.PUBLISH).shouldBe(visible, enabled).click();
    }
}
