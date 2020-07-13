package io.syndesis.qe.validation;

import io.syndesis.qe.util.SftpUtils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assertions;

import java.util.concurrent.TimeUnit;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SftpValidationSteps {

    private final SftpUtils sftpUtils = new SftpUtils();

    @When("put {string} file with content {string} in the directory: {string} using SFTP")
    public void putFileInTheSftpDirectory(String filename, String text, String serverDirectory) {
        sftpUtils.uploadTestFile(filename, text, serverDirectory);
    }

    @Then("validate that file {string} has been transfered from {string} to {string} directory using SFTP")
    public void validateThatFileHasBeenTransferedFromToSftpDirectory(String filename, String serverFromDirectory, String serverToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> sftpUtils.isFileThere(serverToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(sftpUtils.isFileThere(serverFromDirectory, filename)).isFalse();
    }

    @Then("check that {string} file in {string} directory has content {string} using SFTP")
    public void getFileContent(String fileName, String directory, String text) {
        Assertions.assertThat(sftpUtils.getFileContent(directory, fileName)).isEqualTo(text);
    }

    @Given("prepare SFTP server")
    public void prepareSftpServerForTest() {
        sftpUtils.prepareServerForTest();
    }
}
