package io.syndesis.qe.pages.integrations.editor.add.connection;

import static org.assertj.core.api.Assertions.fail;

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
public class ChooseAction extends SyndesisPageObject {

    private static final class Element {

        public static final By ROOT = By.cssSelector(".pf-c-page__main-section > .pf-c-data-list");
        public static final By SELECT_BUTTON = ByUtils.dataTestId("select-action-page-select-button");

        public static final String ACTION_SELECTOR = "li[data-testid=\"integration-editor-actions-list-item-%s-list-item\"]";
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
