package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import java.io.IOException;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.utils.FtpClientManager;
import io.syndesis.qe.utils.FtpUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpValidationSteps {

    private final FtpUtils ftpUtils;

    public FtpValidationSteps() {
        ftpUtils = new FtpUtils(FtpClientManager.getClient());
    }

    @Then("^puts \"([^\"]*)\" file with content \"([^\"]*)\" in the FTP directory: \"([^\"]*)\"$")
    public void putsFileInTheFTPDirectory(String filename, String text, String remoteDirectory) {
        try {
            Assertions.assertThat(ftpUtils.uploadTestFile(filename, text, remoteDirectory)).isTrue();
            Assertions.assertThat(ftpUtils.isThereFile(remoteDirectory + "/" + filename)).isTrue();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }

    @And("^validate that file \"([^\"]*)\" has been transfered to \"([^\"]*)\" FTP directory$")
    public void validateThatFileHasBeenTransferedToDirectory(String filename, String remoteToDirectory) {
        try {
            Assertions.assertThat(ftpUtils.isThereFile(remoteToDirectory + "/" + filename)).isTrue();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }

    @And("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" FTP directory$")
    public void validateFileIsNotThere(String filename, String remoteFromDirectory) {
        try {
            Assertions.assertThat(ftpUtils.isThereFile(remoteFromDirectory + "/" + filename)).isFalse();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" to \"([^\"]*)\" FTP directory$")
    public void validateThatFileHasBeenTransferedFromToDirectory(String filename, String remoteFromDirectory, String remoteToDirectory) {
        try {
            Assertions.assertThat(ftpUtils.isThereFile(remoteFromDirectory + "/" + filename)).isFalse();
            Assertions.assertThat(ftpUtils.isThereFile(remoteToDirectory + "/" + filename)).isTrue();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }
}
