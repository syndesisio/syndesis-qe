package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class OAuthSettingsComponent extends SettingsPage {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-oauth-apps");
        //TODO() here is this "div.alert" element? I cannot find it.
        public static final By ALERT = By.cssSelector("div.alert");
        public static final By SETTINGS_LIST = By.cssSelector("pfng-list");
        public static final By SETTINGS_ITEM = By.cssSelector("div.list-pf-item");
        public static final By SETTINGS_TITLE = By.cssSelector("div.list-pf-title");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    /**
     * @param itemTitle title of settings entry
     * @param formData object where key is name of input field and value it's content
     */
    public void fillSettingsItemForm(String itemTitle, Map<String, String> formData) {
        log.info("filling {} with data '{}'", itemTitle, formData.toString());
        SelenideElement item = this.getSettingsItem(itemTitle);
        for (String key : formData.keySet()){
            SelenideElement input = item.$(String.format("input[name = \"%s\"]", key));
            item.sendKeys(formData.get(key));
        }
    }

    /**
     * Alert should be within the settings item
     *
     * @param itemTitle title of settings item (like Twitter)
     * @returns string content of alert
     */
    public String getAlertText(String itemTitle) {
        SelenideElement item = this.getSettingsItem(itemTitle).shouldBe(visible);
        return item.$(Element.ALERT).shouldBe(visible).getText();
    }

    /**
     * Support method for finding settings items by name
     *
     * @param parent search child elements of this element
     * @returns found elements
     */
    public ElementsCollection listSettingsItems(SelenideElement parent) {
        if (parent == null) {
            parent = getRootElement();
        }
        return parent.$(Element.SETTINGS_LIST).$$(Element.SETTINGS_ITEM);
    }

    /**
     * Fetch all settings items and find proper one according to given name.
     *
     * @param name name of settings item
     * @returns element
     */
    public SelenideElement getSettingsItem(String name) {
        ElementsCollection items = this.listSettingsItems(null).shouldBe(sizeGreaterThan(0));
        log.info("searching for {} in {} items", name, items.size());
        for (SelenideElement item : items) {
            String title = item.$(Element.SETTINGS_TITLE).shouldBe(visible).getText();
            if (name.equals(title)) {
                return item;
            }
        }
        throw new IllegalArgumentException(String.format("item%s not found", name));
    }

    /**
     * Click on button which is child element of given settings item
     *
     * @param settingsItemName name of settings item
     * @param buttonTitle title of button
     * @returns resolved once clicked
     */
    public void clickButton(String settingsItemName, String buttonTitle) {
        this.getButtonFromItem(settingsItemName, buttonTitle).shouldBe(visible).click();
    }

    public boolean checkButtonOfItem(String itemTitle, String buttonTitle) {
        return this.getButtonFromItem(itemTitle, buttonTitle).is(visible);
    }

    public SelenideElement getButtonFromItem(String settingsItemName, String buttonTitle) {
        SelenideElement item = this.getSettingsItem(settingsItemName).shouldBe(visible);
        return item.find(By.cssSelector(String.format("button:contains('%s')", buttonTitle)));
    }

}
