package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.url;

import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;

import io.cucumber.java.en.Then;

public class DocsVerificationSteps {
    final String latestReleasedVersion = "7.10";
    final String currentVersion = "7.11";

    @Then("^check version in about page$")
    public void checkVersion() {
        By versionOnAboutPage = By.cssSelector("[data-testid=\"about-modal-content-version-list-item\"]");
        assertThat($(versionOnAboutPage).shouldBe(visible).getText()).isNotEmpty();
        assertThat($(versionOnAboutPage).getText()).isEqualTo(TestUtils.getSyndesisVersion());
    }

    @Then("verify whether the docs has right version")
    public void verifyDocsVersion() {
        Selenide.switchTo().window(1);
        TestUtils.waitFor(() -> url().contains(".redhat.com"), 1, 15, "URL was not found");
        if (TestUtils.isProdBuild()) {
            assertThat(url().split("/")[6]).isEqualTo(currentVersion);
        } else {
            assertThat(url().split("/")[6]).isEqualTo(latestReleasedVersion);
            By versionOnUserGuide = By.cssSelector(".productnumber");
            assertThat($(versionOnUserGuide).shouldBe(visible).getText()).isNotEmpty();
            assertThat($(versionOnUserGuide).getText()).isEqualTo(latestReleasedVersion);
        }
        Selenide.closeWindow();
    }
}
