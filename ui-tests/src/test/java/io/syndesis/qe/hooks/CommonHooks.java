package io.syndesis.qe.hooks;

import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.templates.AmqTemplate;
import io.syndesis.qe.templates.MysqlTemplate;
import io.syndesis.qe.templates.PublicOauthProxyTemplate;
import io.syndesis.qe.utils.SampleDbConnectionManager;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.WebDriverRunner;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonHooks {

    @Autowired
    CommonSteps cs;

    @AfterStep
    public void afterScreenshot(Scenario scenario) {
        if (scenario.isFailed()) {
            byte[] screenshotAsBytes = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshotAsBytes, "image/png");
        }
    }

    //we can close it after specific scenarios, but it will do nothing if connection == null and I do not know exactly all scenarios which opens DB
    // connection
    //@After("@scenario1,@scenario2")
    @After
    public void closeDBConnection() {
        log.debug("Closing DB connection if it exists");
        SampleDbConnectionManager.closeConnections();
    }

    @After
    public void printPodsWhenFailed(Scenario scenario) {
        if (scenario.isFailed()) {
            TestUtils.printPods();
        }
    }

    @After("@integrations-mqtt,@integrations-amqp-to-amqp,@integrations-openwire-to-openwire")
    public void closeAMQBroker() {
        log.info("Deleting AMQ broker");
        AmqTemplate.cleanUp();
    }

    @After("@publicapi")
    public void cleanPublicApi() {
        log.info("Deleting Public API");
        PublicOauthProxyTemplate.cleanUp();
    }

    @After("@integrations-db-to-db-mysql")
    public void cleanMYSQLserver() {
        log.info("Deleting MYSQL server");
        MysqlTemplate.cleanUp();
    }

    @After("@3scale")
    public void default3scaleAnnotation() {
        log.info("Removing 3scale discovery functionality");
        cs.disable3scaleEnvVar();
    }
}
