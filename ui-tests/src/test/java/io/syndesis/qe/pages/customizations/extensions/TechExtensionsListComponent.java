package io.syndesis.qe.pages.customizations.extensions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TechExtensionsListComponent extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");

        public static final By ITEM = By.cssSelector(".list-group-item.list-view-pf-stacked");
        public static final By ITEM_TITLE = By.className("list-group-item-heading");
        public static final By LIST_WRAPPER = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");
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
      //TODO is this necessary ?
        //  $(Element.LIST_WRAPPER).shouldBe(visible);

        try {
            OpenShiftWaitUtils.waitFor(() ->
                    $$(Element.ITEM).stream()
                    .filter(item -> item.find(Element.ITEM_TITLE).getText().equals(name)).count() > 0, 15*1000);
        } catch (InterruptedException | TimeoutException e) {
            log.error("not found!");
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

    public SelenideElement getActionOnExtensionButton(String name, String action) {
        SelenideElement extension = this.getExtensionItem(name);
        assertThat(extension).isNotNull();
        SelenideElement actionButton = extension.findAll(By.cssSelector(".btn.btn-default")).filter(exactText(action)).first().shouldBe(visible);
        assertThat(actionButton).isNotNull();
        return actionButton;
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
