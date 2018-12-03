package io.syndesis.qe.fragments.common.list;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;
import static org.assertj.core.api.Assertions.fail;

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
        try {
            OpenShiftWaitUtils.waitFor(() -> getRootElement().$$(Element.TITLE).size() > 0, 1000 * 60L);
        } catch (InterruptedException | TimeoutException e) {
            fail("Connection was not loaded in 60s", e);
        }
        return getRootElement().$$(Element.TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1)).find(have(attribute("title", title)));

    }
}
