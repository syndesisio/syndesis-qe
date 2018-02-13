package io.syndesis.qe.pages.settings;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/20/17.
 */
@Slf4j
public class SettingsPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-settings-root");
        public static final By ACTIVE_TAB = By.cssSelector("ul.nav-tabs li.active a");
    }

    @Getter
    private OAuthSettingsComponent oauthSettingsComponent = new OAuthSettingsComponent();

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
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
}
