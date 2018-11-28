package io.syndesis.qe.pages.connections.fragments.list;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.fragments.common.list.CardList;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.utils.TestUtils;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;

public class ConnectionsList extends CardList {


    private static final class Element {
        public static final By TECH_PREVIEW = By.xpath("syndesis-card-tech-preview");
    }

    public ConnectionsList(By rootElement) {
        super(rootElement);
    }

    @Override
    public void invokeActionOnItem(String title, ListAction action) {
        KebabMenu kebabMenu = new KebabMenu(getItem(title).$(By.xpath(".//button")));
        switch (action) {
            case DELETE:
                kebabMenu.open();
                kebabMenu.getItemElement("Delete").shouldBe(visible).click();
                TestUtils.sleepForJenkinsDelayIfHigher(3);
                new ModalDialogPage().getButton("Delete").shouldBe(visible).click();
                break;
            default:
                super.invokeActionOnItem(title, action);
        }
    }

    public boolean isConnectionTechPreview(String title) {
        SelenideElement item = getItem(title).parent().$(Element.TECH_PREVIEW);
        return "Technology Preview".equals(item.getText());
    }
}
