package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.codeborne.selenide.WebDriverRunner;

import javax.imageio.ImageIO;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UITestsHooks {
    @AfterStep(order = 10001)
    public void afterScreenshot(Scenario scenario) {
        if (scenario.isFailed() && WebDriverRunner.hasWebDriverStarted()) {
            scaleDownAndAttachScreenshot(scenario);
            if ("chrome".equals(TestConfiguration.syndesisBrowser())) {
                // log only chrome due to firefox issue https://github.com/SeleniumHQ/selenium/issues/7792
                scenario.attach(WebDriverRunner.getWebDriver().manage().logs().get(LogType.BROWSER)
                        .getAll().stream().map(LogEntry::toString).collect(Collectors.joining("\n")),
                    "text/plain", "Browser console log");
            }
        }
    }

    /**
     * Takes a screenshot using selenium and then resizes it to half size and attaches it to the scenario.
     * @param scenario scenario
     */
    private void scaleDownAndAttachScreenshot(Scenario scenario) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(
            ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES))) {
            BufferedImage i = ImageIO.read(bais);
            BufferedImage scaled = new BufferedImage((int) (i.getWidth() * 0.5), (int) (i.getHeight() * 0.5), i.getType());
            scaled.createGraphics().drawRenderedImage(i, AffineTransform.getScaleInstance(0.5, 0.5));
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(scaled, "png", baos);
                scenario.attach(baos.toByteArray(), "image/png", "Screenshot");
            }
        } catch (IOException e) {
            log.error("Unable to scale down the image: ", e);
            scenario.attach("Unable to process screenshot: " + e, "text/plain", "Screenshot");
        }
    }
}
