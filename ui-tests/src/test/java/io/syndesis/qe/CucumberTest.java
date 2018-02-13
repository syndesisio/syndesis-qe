package io.syndesis.qe;

import com.codeborne.selenide.Configuration;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:features",
		format = {"pretty", "html:target/cucumber/cucumber-html", "junit:target/cucumber/cucumber-junit.xml", "json:target/cucumber/cucumber-report.json"}
)
public class CucumberTest extends TestSuiteParent {

	@BeforeClass
	public static void setupCucumber() {
		//set up Selenide
		Configuration.timeout = 5 * 60 * 1000;
		//We will now use custom web driver
		//Configuration.browser = TestConfiguration.syndesisBrowser();
		Configuration.browser = "io.syndesis.qe.CustomWebDriverProvider";
	}

}
