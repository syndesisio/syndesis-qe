package io.syndesis.qe.pages.integrations.CiCd;

import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManageCICDPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By ITEM_LIST_TAGS = By.className("list-group-item");
        public static final By TAG_NAME = By.className("list-group-item-heading");
        public static final By TAG_USAGE = By.className("list-view-pf-additional-info-item");
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

    public int getNumberOfUsage(String tagName) {
        String textInElement = getElementForTheTag(tagName).find(Element.TAG_USAGE).text();
        Pattern p = Pattern.compile("^Used by (\\d+) integrations$");
        Matcher m = p.matcher(textInElement);
        if (!m.find()) {
            fail("UI label for number of usages `" + textInElement + "` doesn't match pattern.");
        }
        return Integer.parseInt(m.group(1));
    }

}
