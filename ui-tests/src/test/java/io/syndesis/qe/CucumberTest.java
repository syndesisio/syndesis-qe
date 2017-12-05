package io.syndesis.qe;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:features",
		format = {"pretty", "html:target/cucumber-report.html", "junit:target/cucumber-junit.html"}
)
public class CucumberTest {
	// setup
	@BeforeClass
	public static void setup() {
		//set up Selenide
		Configuration.timeout = 5 * 60 * 1000;
		Configuration.browser = TestConfiguration.syndesisBrowser();
		//getWebDriver().manage().window().setSize(new Dimension(1920, 1024));

	}
}
