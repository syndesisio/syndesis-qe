package io.syndesis.qe;

import org.junit.BeforeClass;

import com.codeborne.selenide.Configuration;

public abstract class UITestSuiteParent extends TestSuiteParent {
    @BeforeClass
    public static void setupCucumber() {
        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.syndesisBrowser();
        Configuration.browser = "io.syndesis.qe.CustomWebDriverProvider";
        Configuration.browserSize = "1920x1080";
        //Logging selectors is disabled by default, enable it in test properties if you wish
    }
}
