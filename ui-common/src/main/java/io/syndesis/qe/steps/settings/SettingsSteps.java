package io.syndesis.qe.steps.settings;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.settings.SettingsPage;
import io.syndesis.qe.utils.ByUtils;

import org.assertj.core.api.SoftAssertions;

import com.codeborne.selenide.SelenideElement;

import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Created by sveres on 11/20/17.
 */
public class SettingsSteps {

    private SettingsPage settingsPage = new SettingsPage();

    @Then("^check that settings item \"([^\"]*)\" has button \"(\\w+)\"$")
    public void settingsItemHasButton(String itemTitle, String buttonTitle) {
        assertThat(settingsPage.checkButtonOfItem(itemTitle, buttonTitle)).isTrue();
    }

    @When("^\"(\\w+)\" clicks to the \"(\\w+)\" item \"(\\w+)\" button$")
    public void clickSettingsButton(String userAlias, String itemTitle, String buttonTitle) {
        settingsPage.clickButton(itemTitle, buttonTitle);
    }

    @When("^.*fills? all oauth settings$")
    public void fillAllOAuthSettings() {
        settingsPage.fillAllOAuthSettings();
    }

    @When("^fill \"([^\"]*)\" oauth settings \"([^\"]*)\"")
    public void fillOAuthSettings(String itemTitle, String credential) {
        settingsPage.fillGivenOAuthSetting(settingsPage.getSettingsItem(itemTitle), credential);
    }

    @Then("^check that given \"([^\"]*)\" oauth settings are filled in")
    public void checkThatGivenOauthSettingsAreFilledIn(String itemTitle) {
        assertOauthSettingsPresence(itemTitle, true);
    }

    @Then("^check that given \"([^\"]*)\" oauth settings are not filled in")
    public void checkThatGivenOauthSettingsAreNotFilledIn(String itemTitle) {
        assertOauthSettingsPresence(itemTitle, false);
    }

    private void assertOauthSettingsPresence(String itemTitle, boolean shouldBePresent) {
        SoftAssertions sa = new SoftAssertions();

        SelenideElement item = settingsPage.getSettingsItem(itemTitle);
        item.shouldBe(visible);
        settingsPage.openSettings(itemTitle);

        String title = item.find(SettingsPage.Element.SETTINGS_TITLE).getText();
        SelenideElement clientId = item.find(SettingsPage.Element.CLIENT_ID).shouldBe(visible);
        SelenideElement clientSecret = item.find(SettingsPage.Element.CLIENT_ID).shouldBe(visible);

        if (shouldBePresent) {
            sa.assertThat(clientId.getValue()).as(String.format("OAuth element %s > %s is empty.", title, clientId.getAttribute("name")))
                .isNotEmpty();
            sa.assertThat(clientSecret.getValue()).as(String.format("OAuth element %s > %s is empty.", title, clientSecret.getAttribute("name")))
                .isNotEmpty();
        } else {
            sa.assertThat(clientId.getValue())
                .as(String.format("OAuth element %s > %s is filled with '%s'.", title, clientId.getAttribute("name"), clientId.getValue())).isEmpty();
            sa.assertThat(clientSecret.getValue())
                .as(String.format("OAuth element %s > %s is filled with client secret.", title, clientSecret.getAttribute("name"))).isEmpty();
        }
        settingsPage.closeCurrentlyExpandedSettings();
        sa.assertAll();
    }

    @When("^remove information about OAuth \"([^\"]*)\"$")
    public void clickButtonOfItem(String itemTitle) {
        settingsPage.openSettings(itemTitle);
        settingsPage.clickButton(itemTitle, "Remove");
        //confirm
        ModalDialogPage modalDialogPage = new ModalDialogPage();
        assertThat(modalDialogPage.getTitleText()).isEqualTo("Confirm Remove?");
        assertThat(modalDialogPage.getModalText())
            .contains(String.format("Are you sure you want to remove the OAuth credentials for '%s'?", itemTitle));
        modalDialogPage.getButton("Remove").shouldBe(enabled).click();
        settingsPage.closeCurrentlyExpandedSettings();
    }

    @Then("^check that OAuth fields exists for connection \"([^\"]*)\"$")
    public void checkFieldsExistence(String itemTitle, DataTable fields) {
        SelenideElement item = settingsPage.getSettingsItem(itemTitle);
        item.shouldBe(visible);
        settingsPage.openSettings(itemTitle);

        List<List<String>> dataRows = fields.cells();
        for (List<String> row : dataRows) {
            item.find(ByUtils.dataTestId(row.get(0))).exists();
        }
        settingsPage.closeCurrentlyExpandedSettings();

    }
}
