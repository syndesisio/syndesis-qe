package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AboutPageSteps {
    private static final class Element {
        public static final By ROOT = By.id("root");
        public static final By SYNDESIS_VERSION = ByUtils.dataTestId("about-modal-content-version-list-item");
        public static final By BUILD_ID = ByUtils.dataTestId("about-modal-content-build-id-list-item");
        public static final By COMMIT_ID = ByUtils.dataTestId("about-modal-content-commit-id-list-item");
    }

    @Then("^check version string in about page$")
    public void checkVersionString() {
        String nightlyVersion = System.getProperty("syndesis.nightly.version");
        assertThat($(Element.SYNDESIS_VERSION).shouldBe(visible).getText())
            .isNotEmpty()
            .containsIgnoringCase(nightlyVersion == null ? System.getProperty("syndesis.version") : nightlyVersion);
    }

    @Then("^check that build id exists in about page$")
    public void checkBuildIdString() {
        assertThat($(Element.BUILD_ID).shouldBe(visible).getText())
            .isNotEmpty();
    }

    @Then("^check that commit id exists in about page$")
    public void checkCommitIdString() {
        assertThat($(Element.COMMIT_ID).shouldBe(visible).getText())
            .isNotEmpty();
    }
}
