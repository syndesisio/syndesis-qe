package io.syndesis.qe.fragments.common.list;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.fragments.common.list.actions.ListAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardList extends AbstractUiElementsList {

    private static final class Element {
        public static final By CARD = By.xpath("//*[contains(@class,'card-pf-view-select')]");
        public static final By TITLE = By.xpath("//h2[contains(@class,'card-pf-title')]");
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
        return getTitle(title).$(By.xpath("./ancestor::*[@class='card-pf-body']"));
    }

    @Override
    public SelenideElement getTitle(String title) {
        return getRootElement().$$(Element.TITLE).find(have(attribute("title", title)));

    }

    @Override
    public boolean invokeActionOnItem(String title, ListAction action) {
        switch(action) {
            case CLICK:
                getItem(title).shouldBe(visible).click();
                return true;
            default: return false;
        }
    }
}
