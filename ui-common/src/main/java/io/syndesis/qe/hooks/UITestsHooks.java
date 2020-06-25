package io.syndesis.qe.hooks;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.codeborne.selenide.WebDriverRunner;

import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;

public class UITestsHooks {
    @AfterStep
    public void afterScreenshot(Scenario scenario) {
        if (scenario.isFailed() && WebDriverRunner.hasWebDriverStarted()) {
            byte[] screenshotAsBytes = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshotAsBytes, "image/png", "Screenshot");
        }
    }

}
