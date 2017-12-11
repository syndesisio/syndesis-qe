package io.syndesis.qe;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.syndesis.qe.bdd.CommonSteps;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:features",
		format = {"pretty", "html:target/cucumber-report", "junit:target/cucumber-junit.html", "json:target/cucumber-report.json"}
)
public class CucumberTest {
	// setup
	@BeforeClass
	public static void setup() {
		//set up Selenide
		Configuration.timeout = 5 * 60 * 1000;
		Configuration.browser = TestConfiguration.syndesisBrowser();
		//getWebDriver().manage().window().setSize(new Dimension(1920, 1024));

		if (TestConfiguration.namespaceCleanup()) {
			CommonSteps commonSteps = new CommonSteps();
			commonSteps.cleanNamespace();
			commonSteps.deploySyndesis();
			commonSteps.waitForSyndeisis();
		}

	}
}
