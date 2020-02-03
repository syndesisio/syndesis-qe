package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.SftpUtils;
import io.syndesis.qe.utils.TestUtils;

import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
        assertThat(TestUtils.waitForEvent(r -> r, () -> sftpUtils.isFileThere(serverToDirectory, filename),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        assertThat(sftpUtils.isFileThere(serverFromDirectory, filename)).isFalse();
    }

    @Given("^prepare SFTP server$")
    public void prepareSftpServerForTest() {
        sftpUtils.prepareServerForTest();
    }
}
