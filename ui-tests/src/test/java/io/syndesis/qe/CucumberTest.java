package io.syndesis.qe;

import io.syndesis.qe.hooks.SelectorSnooper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.extern.slf4j.Slf4j;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    extraGlue = {"apicurito.tests.steps"},
    tags = {"not @wip", "not @manual", "not @deprecated", "not @disabled", "not @apicuritoTests"},
    plugin = {
        "pretty",
        "html:target/cucumber/cucumber-html",
        "junit:target/cucumber/cucumber-junit.xml",
        "json:target/cucumber/cucumber-report.json",
        "io.syndesis.qe.cucumber.MailFormatter:target/cucumber/cucumber-mail/",
        //Transformer for Annotations used in our testsuite
        "io.syndesis.qe.hooks.ClassTransformerHook"
    }
)
@Slf4j
public class CucumberTest extends TestSuiteParent {

    @BeforeClass
    public static void setupCucumber() {
        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        Configuration.collectionsTimeout = Configuration.timeout;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.syndesisBrowser();
        Configuration.browser = "io.syndesis.qe.CustomWebDriverProvider";
        Configuration.browserSize = "1920x1080";
        //Logging selectors is disabled by default, enable it in test properties if you wish
        SelectorSnooper.init();
    }

    @AfterClass
    public static void onTestsEnd() {
        SelectorSnooper.finish();
    }
}
