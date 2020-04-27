package io.syndesis.qe.pages.customizations.connectors;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class ApiClientConnectors extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = ByUtils.dataTestId("api-connector-list");
        public static By CONNECTOR_TITLE = ByUtils.dataTestId("api-connector-name");
        public static By CONNECTORS_LIST_ITEM = ByUtils.containsDataTestId("li", "api-connector-list-item");
        public static By DETAIL_BUTTON = ByUtils.dataTestId("a", "api-connector-list-item-details-button");
    }

    private static class Button {
        public static By CREATE_API_CONNECTOR_RIGHT = ByUtils.dataTestId("api-connector-list-view-create-button");
    }

    public void startWizard() {
        $(Button.CREATE_API_CONNECTOR_RIGHT).shouldBe(visible).click();
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(exist);
    }

    public SelenideElement getConnectorItem(String connectorName) {
        final String dataTestid = ("api-connector-list-item-" + connectorName.trim() + "-list-item").replaceAll("[\\s_]", "-").toLowerCase();
        return $(ByUtils.dataTestId(dataTestid)).should(exist);
    }

    @Override
    public boolean validate() {
        ElementsCollection pageTitleCollection = $$(".simple-page-header__title-text").filter(text("API Client Connectors"));
        return pageTitleCollection.size() == 1;
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
        final String dataTestid = ("api-connector-list-item-" + connectorName.trim() + "-list-item").replaceAll("[\\s_]", "-").toLowerCase();
        $(ByUtils.dataTestId(dataTestid)).should(exist).$(Element.DETAIL_BUTTON).shouldBe(visible).click();
    }

    public SelenideElement getDeleteButton(String connectorName) {
        return getConnectorItem(connectorName).$(By.xpath(".//button[contains(.,\"Delete\")]"));
    }
}
