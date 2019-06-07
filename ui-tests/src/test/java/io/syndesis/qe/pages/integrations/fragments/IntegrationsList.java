package io.syndesis.qe.pages.integrations.fragments;

import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.list.RowList;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.integrations.editor.add.connection.ChooseAction;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationsList extends RowList {

    public IntegrationsList(By rootElement) {
        super(rootElement);
    }

    private static final class Element {
        public static final By STATUS = By.cssSelector("span[data-testid=\"integration-status-status-label\"]");
        public static final By STARTING_STATUS = By.className("integration-list-item__additional-info");
        public static final String INTEGRATION_SELECTOR = "div[data-testid=\"integrations-list-item-%s-list-item\"]";
        public static final By VIEW_INTEGRATION = By.cssSelector("a[data-testid=\"integration-actions-view-button\"]");
    }

    private static final class Button {
        public static final By KEBAB = By.cssSelector("button.dropdown-toggle");
    }

    public void invokeActionOnItem(String title, ListAction action) {
        KebabMenu kebabMenu = new KebabMenu(getItem(title).$(By.xpath(".//button")).should(exist));
        switch (action) {
            case VIEW:
                viewIntegration(title);
                break;
            case DELETE:
                kebabMenu.open();
                kebabMenu.getItemElement("Delete").shouldBe(visible).click();
                new ModalDialogPage().getButton("Delete").shouldBe(visible).click();
                break;
            default:
        }
    }

    @Override
    public SelenideElement getItem(String title) {
        final String cssselector = String.format(Element.INTEGRATION_SELECTOR, title.toLowerCase()
            .replaceAll(" ", "-")
            .replaceAll("/", "-")
            .replaceAll("_", "-"));
        return $(By.cssSelector(cssselector)).shouldBe(visible);
    }

    public String getStatus(String title) {
        SelenideElement statusElement = getItem(title).find(Element.STATUS);
        if (statusElement.exists()) {
            return statusElement.getText().trim();
        } else {
            return "";
        }
    }

    public String getStatus(SelenideElement item) {
        return $(Element.STATUS).shouldBe(visible).getText().trim();
    }

    public String getStartingStatus(SelenideElement item) {
        return $(Element.STARTING_STATUS).shouldBe(visible).getText().trim();
    }

    public SelenideElement getKebabButton(SelenideElement item) {
        return $(Button.KEBAB).shouldBe(visible);
    }

    public KebabMenu getKebabMenu(SelenideElement item) {
        return new KebabMenu(item.shouldBe(visible).$(By.xpath(".//button")).should(exist));
    }

    public void viewIntegration(String name) {
        log.info("Searching for integration {} in list", name);
        try {
            OpenShiftWaitUtils.waitFor(() -> $(getItem(name)).is(visible), 15 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Integration in list was not found in 15s.", e);
        }

        $(getItem(name)).$(Element.VIEW_INTEGRATION).click();
    }
}
