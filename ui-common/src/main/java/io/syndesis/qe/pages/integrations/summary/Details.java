package io.syndesis.qe.pages.integrations.summary;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.integrations.fragments.IntegrationsList;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.io.File;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Details extends SyndesisPageObject {

    ModalDialogPage modal = new ModalDialogPage();

    private static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By STATUS = ByUtils.dataTestId("div", "syndesis-integration-status");
        public static final By PUBLISHED_VERSION = By.className("integration-detail-info__status");

        public static final By TITLE = By.className("integration-detail-editable-name");
        public static final By KEBEB_OPEN_MENU = ByUtils.containsDataTestId("-kebab-toggle");
        public static final By KEBAB_DROPDOWN_MENU = By.className("pf-c-dropdown__menu");
        public static final By INFO = By.className("integration-detail-info");
        public static final By INTEGRATION_DESCRIPTION = By.cssSelector("section.integration-description");
        public static final By EXPOSED_URL = By.cssSelector(".integration-exposed-url__list dd input");

        public static final By STEP_ICON_ELEMENT = By.className("integration-steps-horizontal-item");

        public static final By TAB = By.cssSelector("nav.pf-c-nav > ul > li > a");
    }

    public static final class Status {
        public static final String ACTIVE = "Published";
        public static final String INACTIVE = "Unpublished";
        public static final String DRAFT = "Draft";
        public static final String IN_PROGRESS = "In Progress";
    }

    public static final class Actions {
        public static final String EDIT = "Edit Integration";
    }

    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Getter
    private Activity activityTab = new Activity();

    @Getter
    private Metrics metricsTab = new Metrics();

    public boolean validate() {
        return getRootElement().is(visible);
    }

    public String getIntegrationName() {
        return this.getElementText(Element.TITLE);
    }

    public void editIntegration() {
        this.getLink(Actions.EDIT).shouldBe(visible).click();
    }

    public void done() {
        this.getButton("Done").shouldBe(visible).click();
    }

    public String getStatus() {
        return this.getElementText(Element.STATUS);
    }

    public String getStartingStatus() {
        String text = $(IntegrationsList.Element.STARTING_STATUS).shouldBe(visible).getText().trim();
        if (!text.contains("Starting...") || text.contains("Stopping...")) {
            text = $(IntegrationsList.Element.STARTING_STATUS_WITH_PROGRESS_BAR).shouldBe(visible).getText().trim();
        }
        return text;
    }

    public void waitUntilStartingStatusDisappears() {
        TestUtils.waitFor(() -> !$(IntegrationsList.Element.STARTING_STATUS).exists(),
            1, 60,
            "Integration is still in the Starting... phase");
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

    public int getFlowCount() {
        SelenideElement flowsIconElement = getRootElement().findAll(Element.STEP_ICON_ELEMENT).find(Condition.matchText("Flow"));
        return Integer.parseInt(flowsIconElement.getText().replaceAll("\\D+", "")); // extract digit from text e.g. "5 Flows"
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

    public SelenideElement getPublishedVersion() {
        TestUtils.waitFor(() -> !$(By.className("pf-c-spinner__clipper")).exists(), 5, 60, "The integration is still in \"Starting...\" phase after" +
            " 1 minute");
        return $(Element.PUBLISHED_VERSION);
    }

    public String getApiUrl() {
        return $$(Element.EXPOSED_URL).shouldHaveSize(1).first().shouldBe(enabled, visible)
            .getValue();
    }
}
