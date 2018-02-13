package io.syndesis.qe.pages.connections;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.connections.fragments.list.ConnectionsList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Connections extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-connections-list-page");
    }

    @Getter
    private ConnectionsList connectionsList = new ConnectionsList(By.xpath("//syndesis-connections-list"));

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public ElementsCollection getAllConnections() {
        return connectionsList.getItemsCollection();
    }

    public SelenideElement getConnection(String title) {
        return connectionsList.getItem(title);
    }

    public void openConnectionDetail(String title) {
        connectionsList.invokeActionOnItem(title, ListAction.CLICK);
    }

    public void deleteConnection(String title) {
        connectionsList.invokeActionOnItem(title, ListAction.DELETE);
    }

    public void startWizard() {
        new SyndesisRootPage().getButton("Create Connection").shouldBe(visible).click();
    }
}
