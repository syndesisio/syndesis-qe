package io.syndesis.qe.pages.Virtualizations;

import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.concurrent.TimeoutException;

import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO This class need to be separated to the more class according to logic and pages e.g. Virtualization, VirtualizationView,
 * VirtualizationViewEditor etc.
 * In some cases, the virtualizationList is used for Virtualization, and in some cases, it is used for VirtualizationView
 */
@Slf4j
public class Virtualizations extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".pf-c-page__main-section");
        public static final By EDIT_ROOT = By.cssSelector(".pf-c-page__main");
        public static final By EDIT_VIRTUALIZATION = ByUtils.dataTestId("virtualization-list-item-edit-button");
        public static final By EDIT_VIEW = ByUtils.dataTestId("view-list-item-edit-button");
        public static final By TABS = By.className("virtualization-nav-bar");
        public static final By RESULT_SECTION = By.className("sql-client-content__resultsSection");
        public static final By INVALID_INFO = ByUtils.dataTestId("view-list-item-invalid-view");
    }

    VirtualizationList virtualizationList = new VirtualizationList(By.cssSelector(".list-group.list-view-pf.list-view-pf-view"));

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    public SelenideElement getEditRootElement() {
        return $(Element.EDIT_ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public SelenideElement getVirtualization(String name) {
        return virtualizationList.getItem(name);
    }

    public String getVirtualizationDescription(String name) {
        return virtualizationList.getVirtualizationDescription(name);
    }

    public boolean isVirtualizationPresent(String name) {
        log.info("Checking if virtualization {} is present in the list", name);
        SelenideElement virtualization = getVirtualization(name);
        return virtualization.is(visible);
    }

    public void editVirtualization(String name) {
        log.info("Searching for virtualization {} in list", name);
        try {
            OpenShiftWaitUtils.waitFor(() -> $(virtualizationList.getItem(name)).is(visible), 15 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Virtualization in list was not found in 15s.", e);
        }

        virtualizationList.getItem(name).$(Element.EDIT_VIRTUALIZATION).click();
    }

    public void editView(String name) {
        log.info("Searching for virtualization view {} in list", name);
        try {
            OpenShiftWaitUtils.waitFor(() -> $(virtualizationList.getItem(name)).is(visible), 15 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Virtualization in list was not found in 15s.", e);
        }

        virtualizationList.getItem(name).$(Element.EDIT_VIEW).click();
    }

    public void openTab(String tabName) {
        getEditRootElement().$(Element.TABS).$$("li").filter(matchText(tabName)).get(0).click();
    }

    public void setSQLclientParams(DataTable table) {
        for (List<String> dataRow : table.cells()) {
            //Set view
            getEditRootElement().$(ByUtils.dataTestId("view")).selectOption(dataRow.get(0));

            //Set row limit
            fillInput(getEditRootElement().$(ByUtils.dataTestId("rowlimit")), dataRow.get(1));

            //Set row offset if is not empty
            if (!dataRow.get(2).isEmpty()) {
                fillInput(getEditRootElement().$(ByUtils.dataTestId("rowoffset")), dataRow.get(2));
            }
        }
    }

    public int getNumberofRowsSqlClient() {
        String infoText = getEditRootElement().$(Element.RESULT_SECTION).$("i").getText();
        String[] found = infoText.split(" ");
        if (found.length != 4) {
            throw new IllegalArgumentException(String.format("failed to get files number from %s", infoText));
        }
        return Integer.parseInt(found[3]);
    }

    public void makeActionOnVirtualization(String virtuzalition, String action) {
        virtualizationList.invokeActionOnItem(virtuzalition, action);
    }

    public String getVirtualizationStatus(String integrationName) {
        return virtualizationList.getStatus(integrationName);
    }

    public boolean isVirtualizationViewInvalid(String name) {
        return getVirtualization(name).find(Element.INVALID_INFO).is(visible);
    }
}
