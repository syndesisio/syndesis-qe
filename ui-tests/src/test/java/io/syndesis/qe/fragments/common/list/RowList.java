package io.syndesis.qe.fragments.common.list;

import static com.codeborne.selenide.Selenide.$$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RowList extends AbstractUiElementsList {

    private static final class Element {
        public static final By ROW = By.className("pf-c-data-list__item-row");
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
        return $$(Element.ROW).findBy(Condition.text(title));
    }

}
