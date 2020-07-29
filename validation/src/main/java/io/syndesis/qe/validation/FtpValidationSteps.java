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

    @When("put {string} file with content {string} in the directory: {string} using FTP")
    public void putFileInTheFTPDirectory(String filename, String text, String remoteDirectory) {
        ftpUtils.uploadTestFile(filename, text, remoteDirectory);
    }

    @Then("validate that file {string} has been transfered to {string} directory using FTP")
    public void validateThatFileHasBeenTransferedToDirectory(String filename, String remoteToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(remoteToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
    }

    @Given("delete file {string} from FTP")
    public void deleteFile(String path) {
        ftpUtils.deleteFile(path);
    }

    @Then("validate that file {string} has been transfered from {string} directory using FTP")
    public void validateFileIsNotThere(String filename, String remoteFromDirectory) {
        Assertions.assertThat(ftpUtils.isFileThere(remoteFromDirectory, filename)).isFalse();
    }

    @Then("validate that file {string} has been transfered from {string} to {string} directory using FTP")
    public void validateThatFileHasBeenTransferedFromToDirectory(String filename, String remoteFromDirectory, String remoteToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(remoteToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(ftpUtils.isFileThere(remoteFromDirectory, filename)).isFalse();
    }

    @Then("verify that file {string} was created in {string} folder with content {string} using FTP")
    public void validateThatFileWasCreatedWithContent(String filename, String directory, String content) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> ftpUtils.isFileThere(directory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(ftpUtils.isFileThere(directory, filename)).isTrue();
        Assertions.assertThat(ftpUtils.getFileContent(directory, filename)).isEqualTo(content);
    }

    @Then("check that {string} file in {string} directory has content {string} using FTP")
    public void getFileContent(String fileName, String directory, String text) {
        Assertions.assertThat(ftpUtils.getFileContent(directory, fileName)).isEqualTo(text);
    }
}
