package io.syndesis.qe.hooks;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.codeborne.selenide.WebDriverRunner;

import cucumber.api.Scenario;
import cucumber.api.java.After;

public class CommonHooks {

	@After
	public void afterScreenshot(Scenario scenario) {
		byte[] screenshotAsBytes = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
		scenario.embed(screenshotAsBytes, "image/png");
	}
}
