package io.syndesis.qe.pages.settings;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class SettingsPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-settings-root");
        public static final By OAUTH_APPS = By.cssSelector("syndesis-oauth-apps");
        public static final By SETTINGS_LIST = By.cssSelector("pfng-list");
        public static final By SETTINGS_ITEM = By.cssSelector("div.list-pf-item");
        public static final By SETTINGS_TITLE = By.cssSelector("div.list-pf-title");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public SelenideElement getOauthAppsRootElement() {
        return $(Element.OAUTH_APPS).shouldBe(visible);
    }

    public void fillAllOAuthSettings() {
        for (SelenideElement listItem : getSettingsItems()) {
            listItem.shouldBe(visible).click();
            String text = listItem.$(By.className("list-pf-title")).getText();
            switch (text) {
                case "Salesforce":
                    fillOAuthItem(listItem, "QE Salesforce");
                    break;
                case "Twitter":
                    updateTwitterAccount("Twitter Listener");
                    fillOAuthItem(listItem, "Twitter Listener");
                    break;
                case "SAP Concur":
                    fillOAuthItem(listItem, "QE Concur");
                    log.info("Concur oauth has no test yet");
                    break;
                case "Gmail":
                    fillOAuthItem(listItem, "QE Google Mail");
                    break;
                default:
                    Assert.fail("Unknown oauth list item found: " + text + ", can not set it!");
            }
            getButton("Save").shouldBe(visible).click();
            //alert-success should show
            $(By.className("alert-success")).shouldBe(visible);
            //close list item details
            listItem.$(By.className("list-pf-title")).click();
        }
    }

    public void fillOAuthItem(SelenideElement item, String credentialsName) {
        Form form = new Form(item);
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(credentialsName);
        if (optional.isPresent()) {
            form.fillById(optional.get().getProperties());
        } else {
            Assert.fail("Credentials for " + credentialsName + " were not found!");
        }
    }


    public ElementsCollection getSettingsItems() {
        ElementsCollection items = getOauthAppsRootElement().$(Element.SETTINGS_LIST).$$(Element.SETTINGS_ITEM);
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public void updateTwitterAccount(String name) {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(name);
        if (optional.isPresent()) {
            if (!optional.get().getProperties().containsKey("Client Secret")) {

                Map<String, String> additions = new HashMap<>();
                additions.put("clientId", optional.get().getProperty("consumerKey"));
                additions.put("clientSecret", optional.get().getProperty("consumerSecret"));

                optional.get().getProperties().putAll(additions);
            }
        }
    }

    /**
     * Fetch all settings items and find proper one according to given name.
     *
     * @param name name of settings item
     * @returns element
     */
    public SelenideElement getSettingsItem(String name) {
        ElementsCollection items = this.getSettingsItems().shouldBe(sizeGreaterThan(0));
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
     * @param buttonTitle      title of button
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
        return getButton(buttonTitle, item).shouldBe(visible);
    }
}
