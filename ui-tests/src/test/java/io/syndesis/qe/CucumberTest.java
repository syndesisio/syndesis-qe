package io.syndesis.qe;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        tags = {"not @wip", "not @manual", "not @deprecated", "not @disabled", "not @extension"},
        format = {"pretty", "html:target/cucumber/cucumber-html", "junit:target/cucumber/cucumber-junit.xml", "json:target/cucumber/cucumber-report.json"}
)
public class CucumberTest extends TestSuiteParent {

    @BeforeClass
    public static void setupCucumber() {
        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        Configuration.collectionsTimeout = Configuration.timeout;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.syndesisBrowser();
        Configuration.browser = "io.syndesis.qe.CustomWebDriverProvider";
        Configuration.browserSize= "1920x1080";
    }

}
