package io.syndesis.qe.pages.Virtualizations;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.fragments.common.list.RowList;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.ModalDialogPage;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

public class VirtualizationList extends RowList {

    private static final class Element {
        public static final By STATUS = By.className("publish-status-with-progress_Label");
        public static final By DESCRIPTION = By.className("list-group-item-text");
    }

    public VirtualizationList(By rootElement) {
        super(rootElement);
    }

    public String getVirtualizationDescription(String name) {
        return getItem(name).$(Element.DESCRIPTION).getText();
    }

    public void invokeActionOnItem(String title, String action) {
        KebabMenu kebabMenu = new KebabMenu(getItem(title).$(By.xpath(".//button")).should(exist));
        kebabMenu.open();

        //kebab do not work it excludes NO CHILDREN
        getItem(title).parent().$$(By.cssSelector("a")).filter(Condition.exactText(action)).get(0).click();
        //kebabMenu.getItemElement(action).shouldBe(visible).click();

        if ("Delete".equals(action)) {
            new ModalDialogPage().getButton("Delete").shouldBe(visible).click();
        }
    }

    public String getStatus(String title) {
        SelenideElement statusElement = getItem(title).find(Element.STATUS);
        if (statusElement.exists()) {
            return statusElement.getText().trim();
        } else {
            return "";
        }
    }
}
