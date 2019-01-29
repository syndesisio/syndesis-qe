package io.syndesis.qe.pages.integrations.fragments;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.fragments.common.list.RowList;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;

public class OperationsList extends RowList {
    public OperationsList(By rootElement) {
        super(rootElement);
    }

    private static final class Element {
        public static final By TITLE = By.xpath("//*[contains(@class,'list-pf-title')]");
        public static final By STATUS = By.cssSelector(".list-pf-additional-content div");
        public static final By DESCRIPTION = By.className("list-pf-description");
    }

    public String getStatus(String operationName) {
        return getItem(operationName).$(Element.STATUS).shouldBe(visible).getText().trim();
    }

    public String getDescription(String operationName) {
        return getItem(operationName).$(Element.DESCRIPTION).shouldBe(visible).getText().trim();
    }

    @Override
    public SelenideElement getTitle(String title) {
        // operation list does not have the title="..." attribute
        return getRootElement().shouldBe(visible).$$(Element.TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1))
                .find(have(exactText(title)));
    }
}
