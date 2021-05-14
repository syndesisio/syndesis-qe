package io.syndesis.qe;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.codeborne.selenide.WebDriverProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomWebDriverProvider implements WebDriverProvider {
    public static final String DOWNLOAD_DIR = Paths.get("tmp" + File.separator + "download").toAbsolutePath().toString();

    private final String INTEGRATION_EXPORT_MIME_TYPE = "application/octet-stream;application/zip";

    @Override
    public WebDriver createDriver(DesiredCapabilities capabilities) {
        log.info("malphite - I am now inside CustomWebDriverProvider");

        if (TestConfiguration.syndesisBrowser().contentEquals("chrome")) {

            return prepareChromeWebDriver();
        } else {

            // firefox needs to have DOWNLOAD_DIR path already created
            File dirPath = new File(DOWNLOAD_DIR);
            dirPath.mkdirs();

            return prepareFirefoxDriver();
        }
    }

    /**
     * Method will prepare chrome driver with custom profile
     *
     * @return
     */
    private ChromeDriver prepareChromeWebDriver() {
        log.info("setting chrome profile");

        WebDriverManager.chromedriver().setup();
        log.info("Chrome driver version is: " + WebDriverManager.chromedriver().getDownloadedDriverVersion());

        Map<String, Object> preferences = new Hashtable<String, Object>();
        preferences.put("profile.default_content_settings.popups", 0);
        preferences.put("download.prompt_for_download", "false");
        preferences.put("download.default_directory", DOWNLOAD_DIR);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", preferences);
        options.addArguments("--no-sandbox");
        options.addArguments(TestConfiguration.syndesisUrl());
        options.setCapability("acceptInsecureCerts", true);

        TestConfiguration.browserBinary().map(options::setBinary);

        /* this version of constructor is deprecated :( how can we add those DesiredCapabilities? it is not used for now
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        Configuration.browserCapabilities = capabilities;
        return new ChromeDriver(capabilities);
        */

        return new ChromeDriver(options);
    }

    /**
     * Method will prepare firefox driver with custom profile
     *
     * @return
     */
    private FirefoxDriver prepareFirefoxDriver() {
        //so far we only use firefox and chrome so lets set up firefox here
        log.info("setting firefox profile");
        WebDriverManager.firefoxdriver().setup();
        log.info("Firefox driver version is: " + WebDriverManager.firefoxdriver().getDownloadedDriverVersion());

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setCapability("marionette", true);
        firefoxOptions.addArguments(TestConfiguration.syndesisUrl());

        firefoxOptions.addPreference("browser.download.folderList", 2);
        firefoxOptions.addPreference("browser.download.manager.showWhenStarting", false);
        firefoxOptions.addPreference("browser.download.panel.shown", false);
        firefoxOptions.addPreference("browser.download.dir", DOWNLOAD_DIR);
        firefoxOptions.addPreference("browser.download.useDownloadDir", true);
        firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", INTEGRATION_EXPORT_MIME_TYPE);
        firefoxOptions.addPreference("javascript.enabled", true);
        firefoxOptions.addPreference("app.update.enabled", false);
        firefoxOptions.addPreference("app.update.service.enabled", false);
        firefoxOptions.addPreference("app.update.auto", false);
        firefoxOptions.addPreference("app.update.staging.enabled", false);
        firefoxOptions.addPreference("app.update.silent", false);
        firefoxOptions.addPreference("media.gmp-provider.enabled", false);
        firefoxOptions.addPreference("extensions.update.autoUpdate", false);
        firefoxOptions.addPreference("extensions.update.autoUpdateEnabled", false);
        firefoxOptions.addPreference("extensions.update.enabled", false);
        firefoxOptions.addPreference("extensions.update.autoUpdateDefault", false);
        firefoxOptions.addPreference("extensions.logging.enabled", false);
        firefoxOptions.addPreference("lightweightThemes.update.enabled", false);

        return new FirefoxDriver(firefoxOptions);
    }

    /**
     * Cleaning a browser download folder
     */
    public static void cleanDownloadFolder() throws IOException {
        try {
            FileUtils.cleanDirectory(new File(DOWNLOAD_DIR));
        } catch (IllegalArgumentException ex) {
            log.info("Temp download dir for webdriver not found, skip cleaning");
        }
    }
}
