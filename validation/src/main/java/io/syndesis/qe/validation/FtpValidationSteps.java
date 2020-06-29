package io.syndesis.qe.validation;

import io.syndesis.qe.util.FtpUtils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assertions;

import java.util.concurrent.TimeUnit;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpValidationSteps {
    private final FtpUtils ftpUtils = new FtpUtils();

    @When("^put \"([^\"]*)\" file with content \"([^\"]*)\" in the directory: \"([^\"]*)\" using FTP$")
    public void putFileInTheFTPDirectory(String filename, String text, String remoteDirectory) {
        ftpUtils.uploadTestFile(filename, text, remoteDirectory);
    }

    @Then("^validate that file \"([^\"]*)\" has been transfered to \"([^\"]*)\" directory using FTP$")
    public void validateThatFileHasBeenTransferedToDirectory(String filename, String remoteToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(remoteToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
    }

    @Given("^delete file \"([^\"]*)\" from FTP")
    public void deleteFile(String path) {
        ftpUtils.deleteFile(path);
    }

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" directory using FTP$")
    public void validateFileIsNotThere(String filename, String remoteFromDirectory) {
        Assertions.assertThat(ftpUtils.isFileThere(remoteFromDirectory, filename)).isFalse();
    }

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" to \"([^\"]*)\" directory using FTP$")
    public void validateThatFileHasBeenTransferedFromToDirectory(String filename, String remoteFromDirectory, String remoteToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(remoteToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(ftpUtils.isFileThere(remoteFromDirectory, filename)).isFalse();
    }

    @Then("^verify that file \"([^\"]*)\" was created in \"([^\"]*)\" folder with content \'([^\']*)\' using FTP$")
    public void validateThatFileWasCreatedWithContent(String filename, String directory, String content) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(directory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(ftpUtils.isFileThere(directory, filename)).isTrue();
        Assertions.assertThat(ftpUtils.getFileContent(directory, filename)).isEqualTo(content);
    }

    @Then("^check that \"([^\"]*)\" file in \"([^\"]*)\" directory has content \"([^\"]*)\" using FTP$")
    public void getFileContent(String fileName, String directory, String text) {
        Assertions.assertThat(ftpUtils.getFileContent(directory, fileName)).isEqualTo(text);
    }
}
