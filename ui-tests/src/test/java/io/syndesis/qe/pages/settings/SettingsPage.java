package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisPageObject;

import org.junit.Assert;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class SettingsPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By OAUTH_APPS = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");
        public static final By SETTINGS_LIST = By.cssSelector(".list-group.list-view-pf.list-view-pf-view");
        public static final By SETTINGS_ITEM = By.className("list-group-item");
        public static final By SETTINGS_TITLE = By.className("list-group-item-heading");
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
        return $(Element.ROOT).shouldBe(visible);
    }

    public void fillAllOAuthSettings() {
        // previously used foreach lead to stale elements
        for (int i = 0; i < getSettingsItems().size(); i++) {
            SelenideElement listItem = getSettingsItems().get(i);
            String text = listItem.$(Element.SETTINGS_TITLE).getText();
            log.info("Filling in {}", text);
            if ("OpenAPI client".matches(text)) {
                log.info("Skipping OpenAPI client due to bug #5532");
                continue;
            }
            listItem.shouldBe(visible).click();
            String credentialsName = null;
            switch (text) {
                case "Salesforce":
                    credentialsName = "QE Salesforce";
                    break;
                case "Twitter":
                    updateTwitterAccount("Twitter Listener");
                    credentialsName = "Twitter Listener";
                    break;
                case "SAP Concur":
                    credentialsName = "QE Concur";
                    log.info("Concur oauth has no test yet");
                    break;
                case "Gmail":
                    credentialsName = "QE Google Mail";
                    break;
                case "Google Calendar":
                    credentialsName = "QE Google Calendar";
                    break;
                case "Google Sheets":
                    credentialsName = "QE Google Sheets";
                    break;
                default:
                    log.error("Unknown oauth list item found: '" + text + "' !!!");
                    //close listitem
                    listItem.$(By.className("list-pf-title")).click();
                    continue;
            }
            fillGivenOAuthSetting(listItem, credentialsName);
        }
    }

    public void fillOauthSettings(String service, String credentials) {
        fillGivenOAuthSetting(getSettingsItem(service), credentials);
    }

    public void fillGivenOAuthSetting(SelenideElement listItem, String credentialsName) {
        do {
            Selenide.refresh();
            listItem.shouldBe(visible).click();
            fillOAuthItem(listItem, credentialsName);
            getButton("Save").shouldBe(visible).click();
            Selenide.sleep(1000);
        } while ($(By.className("alert-success")).is(not(visible)));
        //close list item details
        getRootElement().$(By.cssSelector("div[class*='list-view-pf-expand active']")).click();
    }

    public void fillOAuthItem(SelenideElement item, String credentialsName) {
        Form form = new Form(item);
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(credentialsName);
        if (optional.isPresent()) {
            Map<String, String> properties = new HashMap<>();
            optional.get().getProperties().forEach((key, value) ->
                properties.put(key.toLowerCase(), value)
            );
            form.fillByTestId(properties);
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
        return getButton(buttonTitle, item).shouldBe(visible);
    }
}
