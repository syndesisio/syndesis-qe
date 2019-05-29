package io.syndesis.qe.pages.integrations;

import static com.codeborne.selenide.Condition.appears;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

public class CiCdDialog extends ModalDialogPage {

    private static final class Element {
        static final By NAME_INPUT = By.cssSelector("input[placeholder=\"Enter a new environment name here...\"]");
        static final By ITEM_LIST_TAGS = By.className("list-group-item");
        static final By CHECKBOX = By.cssSelector("input[type='checkbox']");
        static final By TAG_NAME = By.cssSelector("div[class='list-group-item-heading'] > span");
        static final By ALERT_POP_UP = By.className("alert-danger");
    }

    private static final class Button {
        static final By ADD = By.xpath(".//button[@class='btn btn-primary' and contains(.,'Add')]");
        static final By ADD_NEW = By.xpath(".//a[contains(.,'+ Add new')]");

        static final By EDIT = By.xpath(".//a[contains(.,'Edit')]"); //for every item
        static final By EDIT_ICON_OK = By.className("form-control-pf-save");
        static final By EDIT_ICON_CANCEL = By.className("form-control-pf-cancel");

        static final By REMOVE = By.xpath(".//a[contains(.,'Remove')]"); //for every item
        static final By REMOVE_CANCEL_POP_UP = By.xpath("//button[@class='btn btn-default pull-right' and contains(.,'Cancel')]");
        static final By REMOVE_CONFIRM = By.xpath(".//button[@class='btn btn-danger pull-right' and contains(.,'Remove')]");

        static final By SAVE_DIALOG = By.xpath(".//button[@class='btn btn-primary' and contains(.,'Save')]");
        static final By CANCEL_DIALOG = By.xpath(".//button[@class='btn btn-default' and contains(.,'Cancel')]");
    }

    public void addNewTag(String tagName) {
        if (getAllTags().size() > 0) { // when at least 1 tag exists, the input has to be triggered by ADD NEW link
            getRootElement().find(Button.ADD_NEW).shouldBe(Condition.visible).click();
            waitForDialogIsReady();
            getRootElement().find(Element.NAME_INPUT).waitUntil(appears, 5000);
        }
        getRootElement().find(Element.NAME_INPUT).shouldBe(Condition.visible).setValue(tagName);
        getRootElement().find(Button.ADD).shouldBe(Condition.visible).click();
        waitForDialogIsReady();
    }

    public void updateTag(String tagName, String newTagName) {
        SelenideElement tag = this.getElementForTheTag(tagName);
        tag.find(Button.EDIT).shouldBe(Condition.visible).click();
        tag.find(Button.EDIT_ICON_OK).shouldBe(Condition.disabled);
        tag.find(Button.EDIT_ICON_CANCEL).shouldBe(Condition.enabled);

        tag.find(Element.NAME_INPUT).shouldBe(Condition.visible).setValue(newTagName);
        tag.find(Button.EDIT_ICON_OK).shouldBe(Condition.enabled).click();
        waitForDialogIsReady();
    }

    public void removeTag(String tagName) {
        this.getElementForTheTag(tagName).find(Button.REMOVE).shouldBe(Condition.visible).click();
        SelenideElement popUp = getRootElement().find(Element.ALERT_POP_UP);

        popUp.shouldBe(Condition.visible);
        popUp.find(Button.REMOVE_CANCEL_POP_UP).shouldBe(Condition.visible);

        popUp.find(Button.REMOVE_CONFIRM).shouldBe(Condition.visible).click();
        waitForDialogIsReady();
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
