package io.syndesis.qe.pages.connections.fragments.list;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.fragments.common.list.CardList;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.ModalDialogPage;

public class ConnectionsList extends CardList {

    public ConnectionsList(By rootElement) {
        super(rootElement);
    }

    @Override
    public boolean invokeActionOnItem(String title, ListAction action) {
        if(super.invokeActionOnItem(title, action)) {
            return true;
        } else {
            KebabMenu kebabMenu = new KebabMenu(getItem(title).$(By.xpath(".//button")));
            switch(action) {
                case DELETE:
                    kebabMenu.open();
                    kebabMenu.getItemElement("Delete").shouldBe(visible).click();
                    new ModalDialogPage().getButton("OK").shouldBe(visible).click();
                    return false;
                default:return false;
            }
        }
    }
}
