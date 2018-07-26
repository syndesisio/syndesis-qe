package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.fragments.IntegrationsList;

public class Home extends MainPage {

    IntegrationsList integrationsList = new IntegrationsList(By.cssSelector("syndesis-integration-list"));

    public void checkVisibility(boolean shouldBeVisible) {
        if (shouldBeVisible) {
            $(Tab.HOME).shouldBe(visible);
        } else {
            $(Tab.HOME).shouldNotBe(visible);
        }
    }
}
