package io.syndesis.qe.pages.customizations.extensions;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TechExtensionsListComponent extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");
        public static final String ITEM_SELECTOR = "extension-list-item-%s-list-item";
        public static final String EXTENSION_ACTION_SELECTOR = "extension-list-item-%s-button";
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public static By extensionActionButton(String action) {
        return ByUtils.dataTestId(String.format(Element.EXTENSION_ACTION_SELECTOR, action.toLowerCase()));
    }

    public SelenideElement getExtensionItem(String name) {
        String dataTestId = String.format(Element.ITEM_SELECTOR, name.toLowerCase().replaceAll(" ", "-"));

        try {
            OpenShiftWaitUtils.waitFor(() ->
                $(ByUtils.dataTestId(dataTestId)).is(visible), 6000);
            return $(ByUtils.dataTestId(dataTestId)).shouldBe(visible);
        } catch (InterruptedException | TimeoutException e) {
            log.error("not found!");
            return null;
        }
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

    public SelenideElement getActionOnExtensionButton(String name, String action) {
        SelenideElement extensionElement = this.getExtensionItem(name);
        return extensionElement.$(extensionActionButton(action)).shouldBe(visible);
    }

    public void chooseActionOnExtension(String name, String action) {
        getActionOnExtensionButton(name, action).shouldBe(visible).click();
    }

    public void checkActionOnExtensionButtonEnabled(String name, String action) {
        getActionOnExtensionButton(name, action).shouldBe(enabled).click();
    }

    public void checkActionOnExtensionButtonDisabled(String name, String action) {
        getActionOnExtensionButton(name, action).shouldNotBe(enabled).click();
    }
}
