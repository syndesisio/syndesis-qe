package io.syndesis.qe.pages.integrations.CiCd;

import static com.codeborne.selenide.Condition.appears;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

public class ManageCiCdDialog extends ModalDialogPage {

    private static final class Element {
        static final By ITEM_LIST_TAGS = By.className("list-group-item");
        static final By CHECKBOX = By.cssSelector("input[type='checkbox']");
        static final By TAG_NAME = By.className("list-group-item-heading");
    }

    private static final class Button {
        static final By SAVE_DIALOG = By.xpath(".//button[@class='btn btn-primary' and contains(.,'Save')]");
        static final By CANCEL_DIALOG = By.xpath(".//button[@class='btn btn-default' and contains(.,'Cancel')]");
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
        List<SelenideElement> checkedTags = getRootElement().findAll(Element.ITEM_LIST_TAGS).stream()
            .filter(x -> x.find(Element.CHECKBOX).isSelected())
            .collect(Collectors.toList());
        return checkedTags.stream()
            .map(x -> x.find(Element.TAG_NAME).text())
            .collect(Collectors.toList());
    }

    private SelenideElement getElementForTheTag(String tagName) {
        return getRootElement().findAll(Element.ITEM_LIST_TAGS).stream()
            .filter(x -> x.find(Element.TAG_NAME).getText().equals(tagName))
            .findFirst().get();
    }

    private void waitForDialogIsReady() {
        getRootElement().find(Element.ITEM_LIST_TAGS).waitUntil(appears, 5000);
        TestUtils.sleepIgnoreInterrupt(2000); // needs to wait for items list shows correctly
    }
}
