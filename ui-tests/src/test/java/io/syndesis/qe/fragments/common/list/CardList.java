package io.syndesis.qe.fragments.common.list;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.concurrent.TimeoutException;


import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class CardList extends AbstractUiElementsList {

    private static final class Element {
        public static final By CARD = By.cssSelector("a[data-testid=\"connection-card-details\"]");
        public static final By TITLE = By.cssSelector("h1[data-testid=\"connection-card-title\"]");
    }

    public CardList(By rootElement) {
        super(rootElement);
    }

    @Override
    public ElementsCollection getItemsCollection() {
        ElementsCollection allItems = getRootElement().shouldBe(Condition.visible).findAll(Element.CARD);
        return allItems;
    }

    @Override
    public SelenideElement getItem(String title) {
        return getRootElement().$$(Element.CARD).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1)).find(text(title));
    }

    @Override
    public SelenideElement getTitle(String title) {
        try {
            OpenShiftWaitUtils.waitFor(() -> getRootElement().$$(Element.TITLE).size() > 0, 1000 * 60L);
        } catch (InterruptedException | TimeoutException e) {
            fail("Connection was not loaded in 60s", e);
        }
        return getRootElement().$$(Element.TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1)).find(text(title));

    }
}
