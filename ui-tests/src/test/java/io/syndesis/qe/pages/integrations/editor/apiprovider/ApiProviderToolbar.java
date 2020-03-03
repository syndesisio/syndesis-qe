package io.syndesis.qe.pages.integrations.editor.apiprovider;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiProviderToolbar extends SyndesisPageObject {
    private static final class Element {
        public static final By ROOT = By.cssSelector(".pf-c-page__main-breadcrumb");
        public static final By OPERATIONS_DROPDOWN = By.cssSelector(".operations-dropdown");
        public static final By GO_TO_OPERATION_LIST_BUTTON = ByUtils.dataTestId("editor-toolbar-dropdown-back-button-item-back-button");
        public static final By EDIT_OPENAPI_DEFINITION = ByUtils.partialLinkText("View/Edit API Definition");
    }

    private static final class Button {
        public static final By PUBLISH = By.xpath(".//button[text()[contains(.,'Publish')]]");
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
        getDropDown().click();
        getDropDown()
            .$$(By.cssSelector("a strong"))
            .filter(Condition.text(operationName))
            .shouldHaveSize(1)
            .get(0)
            .click();
    }

    private SelenideElement getDropDown() {
        return getRootElement().$$(By.cssSelector(".pf-c-breadcrumb__item")).findBy(Condition.matchText("Operation.*"));
    }

    public void goToOperationList() {
        getDropDown().click();
        if ($(Element.GO_TO_OPERATION_LIST_BUTTON).exists()) {
            getRootElement().$(Element.GO_TO_OPERATION_LIST_BUTTON).shouldBe(visible).click();
        } else {
            $$(By.className("pf-c-breadcrumb__item")).get(1).click();
        }
    }

    public void editOpenApi() {
        getRootElement().$(Element.EDIT_OPENAPI_DEFINITION).shouldBe(visible).click();
    }

    public void publish() {
        this.getRootElement().$(Button.PUBLISH).shouldBe(visible, enabled).click();
    }
}
