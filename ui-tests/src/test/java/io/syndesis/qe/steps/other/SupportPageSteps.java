package io.syndesis.qe.steps.other;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.SupportPage;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class SupportPageSteps {
    private SupportPage supportPage = new SupportPage();


    @When("^.*downloads? diagnostics for all integrations$")
    public void downloadAllLogs() throws InterruptedException, IOException {
        supportPage.selectAllIntegrationDownload();
        File downloadedLogs = supportPage.downloadZipLogs();
        Assertions.assertThat(downloadedLogs)
                .exists()
                .isFile()
                .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));

        checkDownloadedFileContent(downloadedLogs);
    }

    @When("^.*downloads? diagnostics for \"([^\"]*)\" integration$")
    public void downloadSpecificLogs(String integrationName) throws InterruptedException, IOException {
        supportPage.selectSpecificIntegrationDownload("my-integration");
        File downloadedLogs = supportPage.downloadZipLogs();
        Assertions.assertThat(downloadedLogs)
                .exists()
                .isFile()
                .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));

        checkDownloadedFileContent(downloadedLogs);

    }

    @When("^.*checks? version string$")
    public void checkVersionString() {
        //TODO: what do we want to test here? some property will be required for syndesis version or where can we take it from?
        Assertions.assertThat(supportPage.getVersion())
                .isNotEmpty()
                .isEqualToIgnoringCase("Syndesis: v1.0");
    }


    private void checkDownloadedFileContent(File file) throws IOException {

        try (ZipFile zipFile = new ZipFile(file)) {
            Assertions.assertThat(zipFile)
                    .has(new Condition<>(f -> f.size() > 4,
                            "Inside of zip file should be at least 5 log files???"));

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                log.info("I found this file in downloaded zip: " + zipEntry.getName());
                Assertions.assertThat(zipEntry)
                        .has(new Condition<>(f -> f.getSize() > 0,
                                "Log file should not be empty!"));
            }
        }
    }
}
