package io.syndesis.qe.pages.integrations.summary;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.io.File;
import io.syndesis.qe.pages.ModalDialogPage;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.CollectionCondition;

import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Details extends SyndesisPageObject {

    ModalDialogPage modal = new ModalDialogPage();

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-detail-page");
        public static final By STATUS = By.cssSelector("syndesis-integration-status");
        public static final By TITLE = By.cssSelector("h1 > syndesis-editable-text");
        public static final By KEBEB_OPEN_MENU = By.cssSelector("div.dropdown.dropdown-kebab-pf.pull-right");
        public static final By KEBAB_DROPDOWN_MENU = By.className("dropdown-menu-right");
        public static final By INFO = By.className("integration-detail__info");

        public static final By TAB = By.cssSelector("li.syn-tabs__tab > a");
    }

    public static final class Status {
        public static final String ACTIVE = "Published";
        public static final String INACTIVE = "Unpublished";
        public static final String DRAFT = "Draft";
        public static final String IN_PROGRESS = "In Progress";
    }

    public static final class Actions {
        public static final String START = "Start Integration";
        public static final String STOP = "Stop Integration";
        public static final String DELETE = "Delete Integration";
        public static final String EDIT = "Edit Integration";
    }

    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Getter
    private Activity integrationActivityListComponent = new Activity();

    public boolean validate() {
        return getRootElement().is(visible);
    }

    public String getIntegrationName() {
        return this.getElementText(Element.TITLE);
    }

    public void deleteIntegration() {
        this.getButton(Actions.DELETE).shouldBe(visible).click();
        modal.getButton("OK").shouldBe(visible).click();
    }

    public void editIntegration() {
        this.getButton(Actions.EDIT).shouldBe(visible).click();
    }

    public void done() {
        this.getButton("Done").shouldBe(visible).click();
    }

    public void toggleIntegrationState() {
        String status = getStatus();

        if (status.equals(Status.DRAFT) || status.equals(Status.INACTIVE)) {
            this.getButton(Actions.START).shouldBe(visible).click();
            modal.getButton("OK").shouldBe(visible).click();
        } else if (status.equals(Status.ACTIVE)) {
            this.getButton(Actions.STOP).shouldBe(visible).click();
            modal.getButton("OK").shouldBe(visible).click();
        } else {
            log.error("Integration state {} cant be toggled!", status);
        }
    }

    public String getStatus() {
        return this.getElementText(Element.STATUS);
    }

    public String getIntegrationInfo() {
        return this.getElementText(Element.INFO);
    }

    public SelenideElement getActionButton(String action) {
        return this.getButton(action);
    }

    public File exportIntegration() throws InterruptedException {

        this.getButton("Export").shouldBe(visible).click();

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + this.getIntegrationName() + "-export.zip";

        // wait for download
        Thread.sleep(5000);

        return new File(filePath);
    }

    public void clickOnKebabMenuAction(String action) {
        // open kebab menu
        this.getRootElement().$(Element.KEBEB_OPEN_MENU).shouldBe(visible).click();

        // click on action
        this.getRootElement().$(Element.KEBAB_DROPDOWN_MENU).shouldBe(visible).$$(By.tagName("a"))
                .shouldHave(CollectionCondition.sizeGreaterThanOrEqual(3)).findBy(exactText(action)).shouldBe(visible).click();
    }

    public void selectTab(String tabName) {
        ElementsCollection allTabs = $(Element.ROOT).findAll(Element.TAB).shouldBe(sizeGreaterThan(0));
        allTabs.stream().filter(s -> tabName.equals(s.getText())).findFirst().get().shouldBe(visible).click();
    }
}
