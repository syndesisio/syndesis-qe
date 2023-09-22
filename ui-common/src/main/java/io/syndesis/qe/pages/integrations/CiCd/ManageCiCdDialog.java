package io.syndesis.qe.pages.integrations.CiCd;

import static org.junit.Assert.fail;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ManageCiCdDialog extends ModalDialogPage {

    private static final class Element {
        static final By ALL_ITEMS = ByUtils.containsDataTestId("-selected-input");
        static final String ITEM_LIST_TAGS_SELECTOR = "tag-integration-list-item-%s-selected-input";
        static final By CHECKBOX = ByUtils.dataTestId("tag-integration-list-item-check");
        static final By TAG_NAME = ByUtils.dataTestId("tag-integration-list-item-text");
    }

    private By getListItemSelector(String tagName) {
        return ByUtils.dataTestId(String.format(Element.ITEM_LIST_TAGS_SELECTOR, tagName.toLowerCase().replaceAll(" ", "-")));
    }

    private static final class Button {
        static final By SAVE_DIALOG = ByUtils.dataTestId("button", "tag-integration-dialog-save-button");
        static final By CANCEL_DIALOG = ByUtils.dataTestId("button", "tag-integration-dialog-cancel-button");
    }

    public void checkTag(String tagName) {
        waitForDialogIsReady();
        SelenideElement tag = this.getElementForTheTag(tagName);
        if (!tag.find(Element.CHECKBOX).isSelected()) {
            tag.find(Element.CHECKBOX).click();
        }
    }

    public void uncheckTag(String tagName) {
        waitForDialogIsReady();
        SelenideElement tag = this.getElementForTheTag(tagName);
        if (tag.find(Element.CHECKBOX).isSelected()) {
            tag.find(Element.CHECKBOX).click();
        }
    }

    public void saveDialog() {
        waitForDialogIsReady();
        getRootElement().find(Button.SAVE_DIALOG).shouldBe(Condition.enabled).click();
    }

    public void cancelDialog() {
        waitForDialogIsReady();
        getRootElement().find(Button.CANCEL_DIALOG).shouldBe(Condition.enabled).click();
    }

    public List<String> getAllTags() {
        waitForDialogIsReady();
        return getRootElement().findAll(Element.TAG_NAME).texts();
    }

    public List<String> getOnlyCheckedTags() {
        waitForDialogIsReady();
        List<SelenideElement> checkedTags = getRootElement().findAll(Element.ALL_ITEMS).stream()
            .filter(x -> x.find(Element.CHECKBOX).isSelected())
            .collect(Collectors.toList());
        return checkedTags.stream()
            .map(x -> x.find(Element.TAG_NAME).text())
            .collect(Collectors.toList());
    }

    private SelenideElement getElementForTheTag(String tagName) {
        return getRootElement().find(getListItemSelector(tagName));
    }

    private void waitForDialogIsReady() {
        if (getRootElement().find(ByUtils.dataTestId("tag-integration-dialog-empty-state-manage-cicd-button")).exists()) {
            fail("The Manage CI/CD dialog is empty and doesn't contain any tag");
        }
        getRootElement().find(ByUtils.containsDataTestId("tag-integration-list-item-")).shouldBe(appears, Duration.ofSeconds(5));
        TestUtils.sleepIgnoreInterrupt(2000); // needs to wait for items list shows correctly
    }
}
