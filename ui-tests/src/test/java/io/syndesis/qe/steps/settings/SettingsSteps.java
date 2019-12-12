package io.syndesis.qe.steps.settings;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.settings.SettingsPage;
import io.syndesis.qe.utils.ByUtils;

import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Created by sveres on 11/20/17.
 */
public class SettingsSteps {

    private SettingsPage settingsPage = new SettingsPage();

    @Then("^check that settings item \"([^\"]*)\" has button \"(\\w+)\"$")
    public void settingsItemHasButton(String itemTitle, String buttonTitle) {
        assertThat(settingsPage.checkButtonOfItem(itemTitle, buttonTitle), is(true));
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
        item.shouldBe(visible).click();
        String title = item.$(By.className("list-group-item-heading")).getText();
        SelenideElement expansion = item.$(By.cssSelector(".list-group-item-container.container-fluid"));
        By textOrPassword = ByUtils.dataTestId("clientid");
        ElementsCollection textFields = expansion.$$(textOrPassword).filterBy(Condition.visible);
        for (int i = 0; i < textFields.size(); i++) {
            SelenideElement text = expansion.$$(textOrPassword).filterBy(Condition.visible).get(i);
            if (shouldBePresent) {
                sa.assertThat(text.getValue()).as(String.format("OAuth element %s > %s is empty.", title, text.getAttribute("name"))).isNotEmpty();
            } else {
                sa.assertThat(text.getValue())
                    .as(String.format("OAuth element %s > %s is filled with '%s'.", title, text.getAttribute("name"), text.getValue())).isEmpty();
            }
        }
        sa.assertAll();
    }

    @Then("^check button \"([^\"]*)\" of item \"([^\"]*)\"$")
    public void checkButtonOfItem(String buttonName, String itemTitle) {
        settingsPage.checkButtonOfItem(itemTitle, buttonName);
    }

    @When("^click button \"([^\"]*)\" of item \"([^\"]*)\"$")
    public void clickButtonOfItem(String buttonTitle, String itemTitle) {
        settingsPage.clickButton(itemTitle, buttonTitle);
    }

    @When("^confirm settings removal$")
    public void confirmSettingsRemoval() {
        $(By.className("modal-content")).$(By.cssSelector("button.btn-danger")).click();
    }
}
