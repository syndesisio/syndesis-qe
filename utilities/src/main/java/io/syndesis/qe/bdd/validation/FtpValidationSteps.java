package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import java.io.IOException;

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

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" to \"([^\"]*)\" directory\"$")
    public void validateThatFileHasBeenTransferedFromToDirectory(String filename, String remoteFromDirectory, String remoteToDirectory) {
        try {
            boolean isThereFrom = ftpUtils.isThereFile(remoteFromDirectory + "/" + filename);
            Assertions.assertThat(isThereFrom).isFalse();
            boolean isThereTo = ftpUtils.isThereFile(remoteToDirectory + "/" + filename);
            Assertions.assertThat(isThereTo).isTrue();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }

    @Then("^puts \"([^\"]*)\" file in the FTP \"([^\"]*)\" directory$")
    public void putsFileInTheFTPDirectory(String filename, String remoteDirectory) {
        try {
            boolean result = ftpUtils.upload(String.format("../%s"), remoteDirectory);
            Assertions.assertThat(result).isTrue();
            boolean isThere = ftpUtils.isThereFile(remoteDirectory + "/" + filename);
            Assertions.assertThat(isThere).isTrue();
        } catch (IOException e) {
            Assertions.fail("Error: " + e);
        }
    }
}
