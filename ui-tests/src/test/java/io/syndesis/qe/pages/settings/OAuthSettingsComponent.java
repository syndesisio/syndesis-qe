package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

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
		public static final By ALERT = By.cssSelector("div.alert");
	}

	@Override
	public SelenideElement getRootElement() {
		return super.getRootElement().shouldBe(visible).$(Element.ROOT);
	}

	/**
	 * @param itemTitle title of settings entry
	 * @param formData object where key is name of input field and value it's content
	 */
	public void fillSettingsItemForm(String itemTitle, Map<String, String> formData) throws Exception {
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
	public String getAlertText(String itemTitle) throws Exception {
		SelenideElement item = this.getSettingsItem(itemTitle).shouldBe(visible);
		return item.$(Element.ALERT).shouldBe(visible).getText();
	}
}
