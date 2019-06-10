package io.syndesis.qe.pages.customizations.connectors;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;

public class ApiClientConnectors extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-api-connector-list");
        public static By CONNECTOR_TITLE = By.xpath("//div[@class='list-pf-title']");
        public static By CONNECTORS_LIST_ITEM = By.xpath("//div[@class='list-pf-item']");
    }

    private static class Button {
        public static By CREATE_API_CONNECTOR_RIGHT = By.xpath("//button[text()[contains(.,'Create API Connector')]]");
    }

    public void startWizard() {
        $(Button.CREATE_API_CONNECTOR_RIGHT).shouldBe(visible).click();
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(exist);
    }

    public SelenideElement getConnectorItem(String connectorName) {
        return getRootElement().findAll(Element.CONNECTOR_TITLE).find(text(connectorName)).$(By.xpath("./ancestor::div[@class='list-pf-item']"));
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }

    public boolean isConnectorPresent(String connectorName) {
        try {
            getRootElement().findAll(Element.CONNECTOR_TITLE).find(text(connectorName)).shouldBe(visible);
            return true;
        } catch (WebDriverException wde) {
            return false;
        }
    }

    public boolean isConnectorsListLongAs(int expectedSize) {
        try {
            $$(Element.CONNECTORS_LIST_ITEM).shouldHaveSize(expectedSize);
            return true;
        } catch (WebDriverException wde) {
            return false;
        }
    }

    public void clickConnectorByTitle(String connectorName) {
        $$(Element.CONNECTOR_TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1)).find(text(connectorName)).shouldBe(visible).click();
    }

    public SelenideElement getDeleteButton(String connectorName) {
        return getConnectorItem(connectorName).$(By.xpath(".//button[contains(.,\"Delete\")]"));
    }
}
