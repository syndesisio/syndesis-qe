package io.syndesis.qe.bdd.validation;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpValidationSteps {

    @Then("^validate that file \"([^\"]*)\" has been transfered from \"([^\"]*)\" to \"([^\"]*)\" directory\"$")
    public void validateThatFileHasBeenTransferedFromToDirectory(String arg0, String arg1, String arg2) throws Throwable {
        // TODO(sveres): implementing ftp clinet in java.
        throw new PendingException();
    }

    @Then("^puts \"([^\"]*)\" file in the FTP \"([^\"]*)\" directory$")
    public void putsFileInTheFTPDirectory(String arg0, String arg1) throws Throwable {
        // TODO(sveres): implementing ftp clinet in java.
        throw new PendingException();
    }

}
