package io.syndesis.qe.fragments.common.list;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.CollectionCondition;
import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RowList extends AbstractUiElementsList {

    private static final class Element {
        public static final By ROW = By.xpath("//*[contains(@class,'list-group-item')]");
        public static final By TITLE = By.xpath("//*[contains(@class,'list-group-item-heading')]");
    }

    public RowList(By rootElement) {
        super(rootElement);
    }

    @Override
    public ElementsCollection getItemsCollection() {
        ElementsCollection allItems = getRootElement().findAll(Element.ROW);
        return allItems;
    }

    @Override
    public SelenideElement getItem(String title) {
        return $(Element.ROW).shouldHave(text(title));
    }

    @Override
    public SelenideElement getTitle(String title) {
        return getRootElement().shouldBe(visible).$$(Element.TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1))
                .find(have(text(title)));
    }

    public String getTitleOfItem(SelenideElement item) {
        return item.find(Element.TITLE).shouldBe(visible).getText().trim();
    }
}
