package io.syndesis.qe.pages.integrations.editor.apiprovider;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.enabled;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;

public class ApiProviderToolbar extends SyndesisPageObject {
    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-api-provider-operation-editor-toolbar");
        public static final By OPERATIONS_DROPDOWN = By.id("operationsDropDown");
        public static final By GO_TO_OPERATION_LIST_BUTTON = By.xpath("//button[normalize-space()='Go to Operation List']");
        public static final By EDIT_OPENAPI_DEFINITION = By.xpath("//a[normalize-space()='View/Edit API Definition']");
    }

    private static final class Button {
        public static final By PUBLISH = By.xpath("//button[text()[contains(.,'Publish')]]");
    }

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void goToOperation(String operationName) {
        getRootElement().$(Element.OPERATIONS_DROPDOWN).click();
        getRootElement().$(Element.OPERATIONS_DROPDOWN)
                .$$(By.cssSelector("li strong"))
                .filter(Condition.text(operationName))
                .shouldHaveSize(1)
                .get(0)
                .click();
    }

    public void goToOperationList() {
        getRootElement().$(Element.OPERATIONS_DROPDOWN).click();
        getRootElement().$(Element.GO_TO_OPERATION_LIST_BUTTON).shouldBe(visible).click();
    }

    public void editOpenApi() {
        getRootElement().$(Element.EDIT_OPENAPI_DEFINITION).shouldBe(visible).click();
    }


    public void publish() {
        this.getRootElement().$(Button.PUBLISH).shouldBe(visible, enabled).click();
    }

}
