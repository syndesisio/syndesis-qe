package io.syndesis.qe.pages.integrations.editor.add.connection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class ChooseAction extends SyndesisPageObject {

    private static final class Element {

        public static final By ROOT = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");
        public static final By TITLE = By.className("list-group-item-heading");
        public static final By SELECT_BUTTON = By.cssSelector("[data-testid=\"select-action-page-select-button\"]");

        public static final String ACTION_SELECTOR = "div[data-testid=\"integration-editor-actions-list-item-%s-list-item\"]";

    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void selectAction(String name) {
        log.info("Searching for integration action {}", name);
        try {
            OpenShiftWaitUtils.waitFor(() -> $(getAction(name)).is(visible), 30 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Action element was not found in 30s.", e);
        }

        $(getAction(name)).$(Element.SELECT_BUTTON).click();
    }

    private By getAction(String action) {
        return By.cssSelector(String.format(Element.ACTION_SELECTOR, action.replaceAll(" ", "-").toLowerCase()));
    }
}
