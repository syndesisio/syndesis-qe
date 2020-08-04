package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.codeborne.selenide.WebDriverRunner;

import java.util.stream.Collectors;

import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UITestsHooks {

    @AfterStep(order = 10001)
    public void afterScreenshot(Scenario scenario) {
        if (scenario.isFailed() && WebDriverRunner.hasWebDriverStarted()) {
            byte[] screenshotAsBytes = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshotAsBytes, "image/png", "Screenshot");
            if ("chrome".equals(TestConfiguration.syndesisBrowser())) {
                // log only chrome due to firefox issue https://github.com/SeleniumHQ/selenium/issues/7792
                scenario.attach(WebDriverRunner.getWebDriver().manage().logs().get(LogType.BROWSER)
                        .getAll().stream().map(LogEntry::toString).collect(Collectors.joining("\n")),
                    "text/plain", "Browser console log");
            }
        }
    }
}
