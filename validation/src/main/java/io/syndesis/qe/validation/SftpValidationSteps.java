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

    @When("^put \"([^\"]*)\" file with content \"([^\"]*)\" in the directory: \"([^\"]*)\" using SFTP$")
    public void putFileInTheSftpDirectory(String filename, String text, String serverDirectory) {
        sftpUtils.uploadTestFile(filename, text, serverDirectory);
    }

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" to \"([^\"]*)\" directory using SFTP$")
    public void validateThatFileHasBeenTransferedFromToSftpDirectory(String filename, String serverFromDirectory, String serverToDirectory) {
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> sftpUtils.isFileThere(serverToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        Assertions.assertThat(sftpUtils.isFileThere(serverFromDirectory, filename)).isFalse();
    }

    @Then("^check that \"([^\"]*)\" file in \"([^\"]*)\" directory has content \"([^\"]*)\" using SFTP$")
    public void getFileContent(String fileName, String directory, String text) {
        Assertions.assertThat(sftpUtils.getFileContent(directory, fileName)).isEqualTo(text);
    }

    @Given("^prepare SFTP server$")
    public void prepareSftpServerForTest() {
        sftpUtils.prepareServerForTest();
    }
}
