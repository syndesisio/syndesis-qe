package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class SettingsPage extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-settings-root");
		public static final By ACTIVE_TAB = By.cssSelector("ul.nav-tabs li.active a");
		public static final By SETTINGS_LIST = By.cssSelector("pfng-list");
		public static final By SETTINGS_ITEM = By.cssSelector("div.list-pf-item");
		public static final By SETTINGS_TITLE = By.cssSelector("div.list-pf-title");
	}

	private OAuthSettingsComponent settingsComponent = new OAuthSettingsComponent();

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	/**
	 * Get title of active settings tab
	 *
	 * @returns title of active settings tab
	 */
	public String activeTabText() {
		SelenideElement activeTab = this.getRootElement().shouldBe(visible).$(Element.ACTIVE_TAB).shouldBe(visible);
		return activeTab.getText();
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
	public SelenideElement getSettingsItem(String name) throws Exception {
		ElementsCollection items = this.listSettingsItems(null).shouldBe(sizeGreaterThan(1));
		log.info("searching for {} in {} items", name, items.size());
		for (SelenideElement item : items) {
			String title = item.$(Element.SETTINGS_TITLE).shouldBe(visible).getText();
			if (name.equals(title)) {
				return item;
			}
		}
		throw new Exception(String.format("item%s not found", name));
	}

	/**
	 * Click on button which is child element of given settings item
	 *
	 * @param settingsItemName name of settings item
	 * @param buttonTitle title of button
	 * @returns resolved once clicked
	 */
	public void clickButton(String settingsItemName, String buttonTitle) throws Exception {
		this.getButtonFromItem(settingsItemName, buttonTitle).shouldBe(visible).click();
	}

	public boolean checkButtonOfItem(String itemTitle, String buttonTitle) throws Exception {
		return this.getButtonFromItem(itemTitle, buttonTitle).is(visible);
	}

	public SelenideElement getButtonFromItem(String settingsItemName, String buttonTitle) throws Exception {
		SelenideElement item = this.getSettingsItem(settingsItemName).shouldBe(visible);
		return item.find(By.cssSelector(String.format("button:contains('%s')", buttonTitle)));
	}

	public OAuthSettingsComponent getSettingsComponent() {
		return settingsComponent;
	}

}
