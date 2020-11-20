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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

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
     * Helper function to find out path of web driver which was downloaded with
     * command `mvn webdriverextensions:install-drivers`
     *
     * @return path of currently set web driver
     */
    private String findDriverPath() {

        // plugin will download drivers to user.dir/drivers
        File folder = new File(System.getProperty("user.dir") + "/../drivers");

        //filter to find out which driver we need as we do not know the exact name of driver file
        FilenameFilter fnf = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                Boolean isChrome = TestConfiguration.syndesisBrowser().contentEquals("chrome");

                return !name.contains("version") &&
                    ((isChrome && name.contains("chrome")) || (!isChrome && name.contains("geckodriver")));
            }
        };
        File[] match = folder.listFiles(fnf);

        //if there is more than one driver or zero - something is wrong and we have to exit
        if (match.length != 1) {
            log.error("We found " + match.length + "drivers");
            log.error("Something is wrong with web drivers, did you run `mvn webdriverextensions:install-drivers`" +
                "before running ui-tests?");
            throw new RuntimeException("Problem with setting webdriver");
        }
        //we know that based on filter there will be just one match
        log.info("We found out that you want to use driver: " + match[0].getAbsolutePath());

        return match[0].getAbsolutePath();
    }

    /**
     * Method will prepare chrome driver with custom profile
     *
     * @return
     */
    private ChromeDriver prepareChromeWebDriver() {
        log.info("setting chrome profile");

        System.setProperty("webdriver.chrome.driver", findDriverPath());

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
        System.setProperty("webdriver.gecko.driver", findDriverPath());

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
     * Cleaning webdriver download folder
     */
    public static void cleanDownloadFolder() throws IOException {
        try {
            FileUtils.cleanDirectory(new File(DOWNLOAD_DIR));
        } catch (IllegalArgumentException ex) {
            log.info("Temp download dir for webdriver not found, skip cleaning");
        }
    }
}
