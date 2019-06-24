package io.syndesis.qe.pages.integrations.CiCd;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;

public class ManageCICDPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By ITEM_LIST_TAGS = By.className("list-group-item");
        public static final By TAG_NAME = By.className("list-group-item-heading");
    }

    private static final class Button {
        public static final By ADD_NEW =
            By.cssSelector("[data-testid=\"cicd-list-empty-state-add-new-button\"],[data-testid=\"cicd-list-view-add-new-button\"]");
        public static final By EDIT = By.cssSelector("[data-testid=\"cicd-list-item-create-button\"]");
        public static final By REMOVE = By.cssSelector("[data-testid=\"cicd-list-item-remove-button\"]");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(ManageCICDPage.Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void clickOnAddNewTagButton() {
        getRootElement().find(Button.ADD_NEW).shouldBe(visible).click();
    }

    public void clickOnEditButton(String tagName) {
        SelenideElement tagRow = getElementForTheTag(tagName);
        tagRow.find(Button.EDIT).shouldBe(visible).click();
    }

    public void clickOnRemoveButton(String tagName) {
        SelenideElement tagRow = getElementForTheTag(tagName);
        tagRow.find(Button.REMOVE).shouldBe(visible).click();
    }

    private SelenideElement getElementForTheTag(String tagName) {
        return getRootElement().findAll(ManageCICDPage.Element.ITEM_LIST_TAGS).stream()
            .filter(x -> x.find(ManageCICDPage.Element.TAG_NAME).getText().equals(tagName))
            .findFirst().get();
    }

    public List<String> getAllTags() {
        return getRootElement().findAll(ManageCICDPage.Element.TAG_NAME).texts();
    }
}
