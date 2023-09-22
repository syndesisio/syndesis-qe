package io.syndesis.qe.report.selector;

import io.syndesis.qe.TestConfiguration;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.codeborne.selenide.Driver;
import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.WebElementSelector;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorSnooper {

    private static final Pattern FORBIDDEN_CSS_CHARACTERS = Pattern.compile("[#.]");
    private static final Path SCREEN_SHOT_FOLDER = Paths.get("target/cucumber/screenshots");
    private static String scenarioName;
    private static Set<By> currentSelectors = new HashSet<>();
    private static SelectorUsageReporter reporter = new SelectorUsageReporter();
    private static boolean pauseReporting = false;

    @Before
    public static void before(Scenario scenario) {
        scenarioName = scenario.getUri() + ":" + scenario.getName();
    }

    public static void pauseReporting() {
        pauseReporting = true;
    }

    public static void resumeReporting() {
        pauseReporting = false;
    }

    /**
     * Setup everything required for logging the selectors, also prepares the final destination of the files
     */
    public static void init() {
        if (TestConfiguration.snoopSelectors()) {
            log.info("Overriding WebElementSelector to report selectors");
            log.info("Creating folder {} for storing screenshots", SCREEN_SHOT_FOLDER.toAbsolutePath());
            try {
                File folder = new File(SCREEN_SHOT_FOLDER.toUri());
                folder.mkdirs();
            } catch (Exception e) {
                log.error("Encountered exception while making folder for screenshots", e);
            }
        }
    }

    /**
     * Delegates the message to create reports
     */
    public static void finish() {
        if (TestConfiguration.snoopSelectors()) {
            log.info("Generating selector reports");
            reporter.generateReports();
        }
    }

    /**
     * Highlights WebElement in the webpage and takes a screenshot
     * uses JS alerts to let Selenide know that the Javascript is done
     *
     * @param el element to highlight
     * @return path to the generated screenshot
     */
    private static String highlightElement(WebElement el) {
        String originalBorder = Selenide.executeJavaScript("arguments[0].style.border", el);
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(3));
        Selenide.executeJavaScript("arguments[0].style.border=\"2px solid red\"", el);
        wait.until(ExpectedConditions.attributeContains(el, "style", "2px solid red"));
        File tmpImage = Screenshots.takeScreenShotAsFile();
        String fileName = Instant.now().toString() + ".png";
        String path = SCREEN_SHOT_FOLDER.resolve(fileName).toString();
        try {
            FileUtils.copyFile(tmpImage, FileUtils.getFile(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //There is no way to reliably wait for inline style removal/being changed, this is more reliable, although ugly when you look at it running
        Selenide.executeJavaScript("arguments[0].style.border=arguments[1]; alert(\"I love javascript\");", el, originalBorder);
        wait.until(ExpectedConditions.alertIsPresent());
        Selenide.switchTo().alert().accept();
        return fileName;
    }

    private static void reportWrongSelector(By selector, WebElement el) {
        //This causes the selector to be reported just once, but also to keep track of all the scenarios it is used in
        if (reporter.wasSelectorReported(selector)) {
            reporter.selectorIsUsedInScenario(selector, scenarioName);
            return;
        }
        log.warn("Found selector which doesn't use data-testid {}", selector);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            //Find call source of the selector
            if (stackTraceElement.getClassName().contains("io.syndesis.qe") && !stackTraceElement.getClassName().contains("SelectorSnooper")) {
                log.warn("This selector is called from {}", stackTraceElement);
                String imgPath = highlightElement(el);
                SelectorUsageInfo info =
                    new SelectorUsageInfo(selector.toString(), WebDriverRunner.url(), Sets.newHashSet(scenarioName), stackTraceElement.toString(),
                        imgPath);
                String dataTestId = Selenide.$(el).data("testid");
                if (dataTestId != null) {
                    info.setDataTestId(dataTestId);
                }
                reporter.report(info);
                break;
            }
        }
    }

    private static boolean usesForbiddenCssCharacters(String id) {
        Matcher matcher = FORBIDDEN_CSS_CHARACTERS.matcher(id);
        return matcher.matches();
    }
}
