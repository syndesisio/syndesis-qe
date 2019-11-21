package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.url;

import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;

import java.util.HashMap;

import cucumber.api.java.en.Then;

public class DocsVerificationSteps {
    private HashMap<String, String> versions = new HashMap<>();
    private static String versionOnAboutPage;

    public DocsVerificationSteps() {
        versions.put("1.9", "7.6");
        versions.put("1.10", "7.7");
    }

    @Then("^check version in about page$")
    public void checkVersion() {
        By syndesisVersion = By.cssSelector("[data-testid=\"about-modal-content-version-list-item\"]");
        assertThat($(syndesisVersion).shouldBe(visible).getText())
            .isNotEmpty();
        assertThat($(syndesisVersion).getText()).isEqualTo(TestUtils.getSyndesisVersion());
        if (Character.isDigit($(syndesisVersion).getText().charAt(3))) {
            versionOnAboutPage = $(syndesisVersion).getText().substring(0, 4);
        } else {
            versionOnAboutPage = $(syndesisVersion).getText().substring(0, 3);
        }
    }

    @Then("verify whether the docs has right version")
    public void verifyDocsVersion() {
        Selenide.switchTo().window(1);
        TestUtils.waitFor(() -> url().contains(".redhat.com"), 1, 15, "URL was not found");
        assertThat(versions.get(versionOnAboutPage)).isEqualTo(url().split("/")[6]);
        Selenide.closeWindow();
    }
}
