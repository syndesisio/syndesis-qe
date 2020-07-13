package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class SettingsPage extends SyndesisPageObject {

    public static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By SETTINGS_LIST = By.cssSelector(".pf-c-data-list");
        public static final By SETTINGS_ITEM = By.className("pf-c-data-list__item");
        public static final By SETTINGS_TITLE = By.id("app-name");
        public static final By EXPAND_BUTTON = By.cssSelector(".pf-c-data-list__toggle");
        public static final By CURRENTLY_EXPANDED = By.cssSelector("*[aria-expanded=\"true\"]");
        public static final By CLIENT_ID = ByUtils.dataTestId("clientid");
        public static final By CLIENT_SECRET = ByUtils.dataTestId("clientsecret");
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
            openSettings(listItem);
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
        openSettings(listItem);
        fillOAuthItem(listItem, credentialsName);
        getButton("Save").shouldBe(visible).click();
        Selenide.sleep(1000);
        //close list item details
        closeCurrentlyExpandedSettings();
    }

    public void fillOAuthItem(SelenideElement item, String credentialsName) {
        Form form = new Form(item);
        Account account = AccountsDirectory.getInstance().get(credentialsName);
        Map<String, String> properties = new HashMap<>();
        account.getProperties().forEach((key, value) ->
            properties.put(key.toLowerCase(), value)
        );
        form.fillByTestId(properties);
    }

    public ElementsCollection getSettingsItems() {
        ElementsCollection items = getOauthAppsRootElement().$(Element.SETTINGS_LIST).$$(Element.SETTINGS_ITEM);
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public void updateTwitterAccount(String name) {
        Account account = AccountsDirectory.getInstance().get(name);
        if (!account.getProperties().containsKey("Client Secret")) {

            Map<String, String> additions = new HashMap<>();
            additions.put("clientId", account.getProperty("consumerKey"));
            additions.put("clientSecret", account.getProperty("consumerSecret"));

            account.getProperties().putAll(additions);
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

    public void openSettings(String connectionName) {
        openSettings(getSettingsItem(connectionName));
    }

    private void openSettings(SelenideElement connectionItem) {
        if (!connectionItem.find(Element.CURRENTLY_EXPANDED).exists()) {
            connectionItem.shouldBe(visible).$(Element.EXPAND_BUTTON).click();
        }
    }

    public void closeCurrentlyExpandedSettings() {
        getRootElement().$(Element.CURRENTLY_EXPANDED).click();
    }

    public boolean checkButtonOfItem(String itemTitle, String buttonTitle) {
        return this.getButtonFromItem(itemTitle, buttonTitle).is(visible);
    }

    public SelenideElement getButtonFromItem(String settingsItemName, String buttonTitle) {
        SelenideElement item = this.getSettingsItem(settingsItemName).shouldBe(visible);
        return getButton(buttonTitle, item).shouldBe(visible);
    }
}
