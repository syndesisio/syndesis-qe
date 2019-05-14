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
        public static final By ACTION = By.cssSelector(".list-group-item.list-view-pf-stacked");
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
            OpenShiftWaitUtils.waitFor(() -> $(Element.ROOT).$$(Element.TITLE)
                    .filterBy(Condition.text(name)).size() == 1, 30 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Action element was not found in 30s.", e);
        }

        $(Element.ROOT).$$(Element.ACTION).filterBy(Condition.text(name))
                .shouldHaveSize(1).first().shouldBe(visible).$(By.tagName("a")).click();
    }
}
