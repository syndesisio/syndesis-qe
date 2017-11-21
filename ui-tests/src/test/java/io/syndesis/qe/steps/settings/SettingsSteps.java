package io.syndesis.qe.steps.settings;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.settings.OAuthSettingsComponent;
import io.syndesis.qe.pages.settings.SettingsPage;

/**
 * Created by sveres on 11/20/17.
 */
public class SettingsSteps {

	private SettingsPage settingsPage = new SettingsPage();

	@Then("^\"(\\w+)\" is presented with \"(\\w+)\" settings tab$")
	public void activeTab(String user, String tabName) {
		//TODO(sveres): find out why there is parameter user?
		String activeTabString = settingsPage.activeTabText();
		assertThat(activeTabString, is(tabName));
	}

	@Then("^settings item \"(\\w+)\" has button \"(\\w+)\"$")
	public void settingsItemHasButton(String itemTitle, String buttonTitle) {
		assertThat(settingsPage.getOauthSettingsComponent().checkButtonOfItem(itemTitle, buttonTitle), is(true));
	}

	@When("^\"(\\w+)\" clicks to the \"(\\w+)\" item \"(\\w+)\" button$")
	public void clickSettingsButton(String userAlias, String itemTitle, String buttonTitle) {
		settingsPage.getOauthSettingsComponent().clickButton(itemTitle, buttonTitle);
	}

	@When("^fill form in \"(\\w+)\" settings item$")
	public void fillSettingsItemForm(String itemTitle) {
		//TODO(dsimansk: )
		//Map<String, String> toFill = this.world.testConfig.settings[itemTitle];
		//settings.fillSettingsItemForm(itemTitle, toFill);
	}

	@Then("^settings item \"(\\w+)\" must have alert with text \"(\\w+)\"$")
	public void assertSettingsAlertText(String itemTitle, String alertText) {
		OAuthSettingsComponent settings = settingsPage.getOauthSettingsComponent();
		assertThat(settings.getAlertText(itemTitle), containsString(alertText));
	}
}
