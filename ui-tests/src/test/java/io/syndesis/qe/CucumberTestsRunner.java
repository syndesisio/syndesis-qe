package io.syndesis.qe;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.openqa.selenium.Dimension;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",
		glue = {"io/syndesis/qe/steps"},
		monochrome = true,
		format = {"pretty"}
)
public class CucumberTestsRunner {
	// setup
	@BeforeClass
	public static void setup() {
		//set up Selenide
		Configuration.timeout = 60000;
		getWebDriver().manage().window().setSize(new Dimension(1920, 1024));
	}
}
