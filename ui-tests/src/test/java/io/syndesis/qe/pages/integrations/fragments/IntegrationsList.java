package io.syndesis.qe.pages.integrations.fragments;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.fragments.common.list.RowList;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.ModalDialogPage;

public class IntegrationsList extends RowList {

    public IntegrationsList(By rootElement) {
        super(rootElement);
    }

    private static final class Element {
        public static final By ITEM = By.xpath("//*[contains(@class,'list-pf-item')]");
        public static final By STATUS = By.xpath(".//div[contains(@class,'status')]/span");
        public static final By DESCRIPTION = By.className("description");
    }

    private static final class Button {
        public static final By KEBAB = By.cssSelector("button.dropdown-toggle");
    }

    @Override
    public void invokeActionOnItem(String title, ListAction action) {
        KebabMenu kebabMenu = new KebabMenu(getItem(title).$(By.xpath(".//button")));
        switch (action) {
            case DELETE:
                kebabMenu.open();
                kebabMenu.getItemElement("Delete").shouldBe(visible).click();
                new ModalDialogPage().getButton("OK").shouldBe(visible).click();
                break;
            default:
                super.invokeActionOnItem(title, action);
        }
    }

    public String getStatus(String title) {
        return getItem(title).find(Element.STATUS).getText().trim();
    }

    public String getStatus(SelenideElement item) {
        return $(Element.STATUS).getText().trim();
    }

    public SelenideElement getKebabButton(SelenideElement item) {
        return $(Button.KEBAB);
    }

    public String getDescription(SelenideElement item) {
        return $(Element.DESCRIPTION).getText().trim();
    }

    public KebabMenu getKebabMenu(SelenideElement item) {
        return new KebabMenu(item.$(By.xpath(".//button")));
    }
}
