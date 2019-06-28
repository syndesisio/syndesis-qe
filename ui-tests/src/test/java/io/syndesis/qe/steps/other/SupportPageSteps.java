package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.SupportPage;

import org.assertj.core.api.Condition;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupportPageSteps {
    private SupportPage supportPage = new SupportPage();

    @When("^.*downloads? diagnostics for all integrations$")
    public void downloadAllLogs() throws InterruptedException, IOException {
        supportPage.selectAllIntegrationDownload();
        File downloadedLogs = supportPage.downloadZipLogs();
        assertThat(downloadedLogs)
            .exists()
            .isFile()
            .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));

        checkDownloadedFileContent(downloadedLogs);
    }

    @When("^.*downloads? diagnostics for \"([^\"]*)\" integration$")
    public void downloadSpecificLogs(String integrationName) throws InterruptedException, IOException {
        supportPage.selectSpecificIntegrationDownload("my-integration");
        File downloadedLogs = supportPage.downloadZipLogs();
        assertThat(downloadedLogs)
            .exists()
            .isFile()
            .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));

        checkDownloadedFileContent(downloadedLogs);
    }

    private void checkDownloadedFileContent(File file) throws IOException {

        try (ZipFile zipFile = new ZipFile(file)) {
            assertThat(zipFile)
                .has(new Condition<>(f -> f.size() > 4,
                    "Inside of zip file should be at least 5 log files???"));

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                log.info("I found this file in downloaded zip: " + zipEntry.getName());
                assertThat(zipEntry)
                    .has(new Condition<>(f -> f.getSize() > 0,
                        "Log file should not be empty!"));
            }
        }
    }
}
