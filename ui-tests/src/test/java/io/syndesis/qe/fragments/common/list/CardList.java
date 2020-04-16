package io.syndesis.qe.fragments.common.list;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardList extends AbstractUiElementsList {

    private static final class Element {
        //Each card has different data-testid, so this selector looks up all elements that contain connection-card and restricts it to articles
        //which are used for the connection cards
        public static final By CARD = ByUtils.containsDataTestId("article", "connection-card");
        public static final By TITLE = ByUtils.dataTestId("h1", "connection-card-title");
    }

    public CardList(By rootElement) {
        super(rootElement);
    }

    @Override
    public ElementsCollection getItemsCollection() {
        ElementsCollection allItems = getRootElement().shouldBe(visible).findAll(Element.CARD);
        return allItems;
    }

    @Override
    public SelenideElement getItem(String title) {
        return getRootElement().$$(Element.CARD).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1)).find(text(title));
    }

    public void invokeActionOnItem(String title, ListAction action) {
        switch (action) {
            case CLICK:
                getItem(title).shouldBe(visible).click();
                break;
            default:
        }
    }
}
