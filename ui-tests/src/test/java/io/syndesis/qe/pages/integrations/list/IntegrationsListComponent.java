package io.syndesis.qe.pages.integrations.list;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.io.File;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class IntegrationsListComponent extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integrations-list");

        public static final By ITEM = By.className("list-pf-item");
        public static final By ITEM_TITLE = By.className("list-pf-title");
        public static final By ITEM_STATUS = By.cssSelector("syndesis-integration-status");
        public static final By ITEM_DESCRIPTION = By.className("description");
        public static final By FILE_INPUT = By.cssSelector("input[type='file']");
        public static final By FINISHED_PROGRESS_BAR = By.cssSelector("*[style='width: 100%;']");
    }

    private static final class Link {
        public static final By KEBAB_DELETE = By.linkText("Delete");
    }

    private static final class Button {
        public static final By OK = By.xpath("//button[.='OK']");
        public static final By KEBAB_DROPDOWN = By.cssSelector("button.dropdown-toggle");

    }

    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    public boolean validate() {
        return getRootElement().is(visible);
    }

    public SelenideElement getIntegration(String name) {
        SelenideElement entryElement = getElementContainingText(Element.ITEM_TITLE, name);
        // this is pure hack to get parent elements as title si too low-level
        //TODO: refactor getElementContainingText to accept params e.g. elementToReturn, elementWithText, text
        return entryElement.parent().parent();
    }

    public boolean isIntegrationPresent(String name) {
        log.info("Checking if integration {} is present in the list", name);
        SelenideElement integration = this.getIntegration(name);
        return integration.is(visible);
    }

    public void goToIntegrationDetail(String integrationName) {
        this.getIntegration(integrationName).shouldBe(visible).click();
    }

    public SelenideElement getIntegrationByStatus(String status) {
        SelenideElement integrationByStatus = getElementContainingText(Element.ITEM_STATUS, status);
        return integrationByStatus;
    }

    public void clickDeleteIntegration(String integrationName) {
        log.info("clicking delete link for integration {}", integrationName);

        this.getRootElement().find(Element.ITEM).shouldBe(visible);

        SelenideElement parentElement = null;
        ElementsCollection parentElements = this.getAllIntegrations();

        for (SelenideElement element : parentElements) {
            String name = getIntegrationName(element);
            if (name.equals(integrationName)) {
                parentElement = element;
                break;
            }
        }

        if (parentElement != null) {
            parentElement.find(Button.KEBAB_DROPDOWN).shouldBe(visible).click();
        }

        this.getRootElement().find(Link.KEBAB_DELETE).shouldBe(visible).click();

        SelenideElement okButton = this.getRootElement().find(Button.OK);

        okButton.shouldBe(visible).click();
    }

    public ElementsCollection getAllIntegrations() {
        return this.getRootElement().findAll(Element.ITEM);
    }

    public String getIntegrationName(SelenideElement integration) {
        String name;
        SelenideElement nameElement = integration.find(Element.ITEM);
        boolean isNamePresent = nameElement.is(visible);

        if (isNamePresent) {
            name = nameElement.getText();
        } else {
            log.warn("Name is not present!");
            name = integration.find(Element.ITEM_DESCRIPTION).shouldBe(visible).getText();
        }

        return name;
    }

    public String getIntegrationItemStatus(SelenideElement item) {
        return item.find(Element.ITEM_STATUS).shouldBe(visible).getText();
    }

    public SelenideElement getKebabButtonFromItem(SelenideElement item) {
        return item.find(Button.KEBAB_DROPDOWN);
    }

    SelenideElement getKebabElement(boolean isOpen, SelenideElement item) {
        String open = isOpen ? ".open" : "";
        return item.find(By.cssSelector("div.dropdown.dropdown-kebab-pf.pull-right" + open));
    }

    public void checkIfKebabHasWhatShouldHave(SelenideElement item, String status) {
        String[] properActions = this.getKebabActionsByStatus(status);

        if (properActions.length == 0) {
            throw new Error("Wrong status!");
        }

        //log.debug(`checking kebab menu of kebab element:`);
        SelenideElement kebabE = this.getKebabElement(true, item);
        kebabE.shouldBe(visible);

        for (String action : properActions) {
            kebabE.find(By.linkText(action)).isDisplayed();
        }
    }

    public String[] getKebabActionsByStatus(String status) {
        String[] actions;

        String view = "View";
        String edit = "Edit";
        String deactivate = "Deactivate";
        String activate = "Activate";
        String deleteAction = "Delete";

        switch (status) {
            case "Active":
                actions = new String[] {view, edit, deactivate, deleteAction};
                break;
            case "Inactive":
            case "Draft":
                actions = new String[] {view, edit, activate, deleteAction};
                break;
            case "In Progress":
                actions = new String[] {view, edit, deleteAction};
                break;
            default:
                actions = new String[] {};
                break;
        }

        return actions;
    }

    public void checkAllIntegrationsKebabButtons() {
        ElementsCollection integrationsItems = getAllIntegrations();

        for (SelenideElement item : integrationsItems) {
            String status = this.getIntegrationItemStatus(item);

            if (status.equals("Deleted")) {
                this.getKebabButtonFromItem(item).shouldBe(hidden);
            } else {
                SelenideElement kebabB = this.getKebabButtonFromItem(item);
                kebabB.shouldBe(visible).click();

                this.checkIfKebabHasWhatShouldHave(item, status);
            }
        }
    }

    public boolean importIntegration(String integrationName) throws InterruptedException {

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + integrationName + "-export.zip";

        File exportedIntegrationFile = new File(filePath);

        ModalDialogPage modal = new ModalDialogPage();
        modal.getRootElement().find(Element.FILE_INPUT).shouldBe(visible).uploadFile(exportedIntegrationFile);

        modal.getElementRandom(Element.FINISHED_PROGRESS_BAR).shouldBe(visible);

        modal.getButton("OK").shouldBe(visible).click();

        return this.getIntegration(integrationName).exists();
    }
}
