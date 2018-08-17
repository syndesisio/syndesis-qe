package io.syndesis.qe.pages.customizations.extensions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class TechExtensionsListComponent extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-tech-extensions-list");

        public static final By ITEM = By.className("list-pf-item");
        public static final By ITEM_TITLE = By.className("list-pf-title");
        public static final By LIST_WRAPPER = By.cssSelector("pfng-list");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public SelenideElement getExtensionItem(String name) {
        $(Element.LIST_WRAPPER).shouldBe(visible);

        try {
            OpenShiftWaitUtils.waitFor(() ->
                    $$(Element.ITEM).stream()
                    .filter(item -> item.find(Element.ITEM_TITLE).getText().equals(name)).count() > 0, 15*1000);
        } catch (InterruptedException | TimeoutException e) {
            return null;
        }

        ElementsCollection items = $$(Element.ITEM);
        return items.stream()
                .filter(item -> item.find(Element.ITEM_TITLE).getText().equals(name))
                .findFirst().get();
    }

    public boolean isExtensionPresent(String name) {
        log.info("Checking if extension {} is present in the list", name);
        SelenideElement extension = this.getExtensionItem(name);
        if (extension != null) {
            return extension.is(visible);
        } else {
            return false;
        }
    }

    public void chooseActionOnExtension(String name, String action) {
        SelenideElement extension = this.getExtensionItem(name);
        SelenideElement actionButton = extension.findAll(By.tagName("button")).filter(exactText(action)).first();
        assertThat(actionButton).isNotNull();
        actionButton.shouldBe(visible).click();
    }
}
