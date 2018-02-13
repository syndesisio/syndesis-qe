package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.connections.list.ConnectionsListComponent;
import lombok.Getter;

public class IntegrationConnectionSelectComponent extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integrations-connection-select");
    }

    @Getter
    private ConnectionsListComponent connectionsListComponent = new ConnectionsListComponent();

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

}
