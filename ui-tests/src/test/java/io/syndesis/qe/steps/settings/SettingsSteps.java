package io.syndesis.qe.steps.settings;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

import static org.hamcrest.core.StringContains.containsString;

import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.settings.OAuthSettingsComponent;
import io.syndesis.qe.pages.settings.SettingsPage;

/**
 * Created by sveres on 11/20/17.
 */
public class SettingsSteps {

	private SettingsPage settingsPage = new SettingsPage();
	private OAuthSettingsComponent settings = new OAuthSettingsComponent();

	@Then("^\"(\\w+)\" is presented with \"(\\w+)\" settings tab$")
	public void activeTab(String user, String tabName) {
		//TODO(sveres): find out why there is parameter user?
		String activeTabString = settingsPage.activeTabText();
		assertThat(activeTabString, is(tabName));
	}

	@Then("^settings item \"(\\w+)\" has button \"(\\w+)\"$")
	public void settingsItemHasButton(String itemTitle, String buttonTitle) throws Exception {
		assertThat(settingsPage.checkButtonOfItem(itemTitle, buttonTitle), is(true));
	}

	@When("^\"(\\w+)\" clicks to the \"(\\w+)\" item \"(\\w+)\" button$")
	public void clickSettingsButton(String userAlias, String itemTitle, String buttonTitle) throws Exception {
		settingsPage.clickButton(itemTitle, buttonTitle);
	}

	@When("^fill form in \"(\\w+)\" settings item$")
	public void fillSettingsItemForm(String itemTitle) throws Exception {
		//TODO(dsimansk: )
		//Map<String, String> toFill = this.world.testConfig.settings[itemTitle];
		//settings.fillSettingsItemForm(itemTitle, toFill);
	}

	@Then("^settings item \"(\\w+)\" must have alert with text \"(\\w+)\"$")
	public void assertSettingsAlertText(String itemTitle, String alertText) throws Exception {
		OAuthSettingsComponent settings = settingsPage.getSettingsComponent();
		assertThat(settings.getAlertText(itemTitle), containsString(alertText));
	}
}
