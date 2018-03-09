package io.syndesis.qe.steps.other;

import com.dropbox.core.DbxException;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.DropBoxUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DropBoxSteps {
    @Lazy
    @Autowired
    private DropBoxUtils dropBoxUtils;

    @When("^.*uploads? file with path \"([^\"]*)\" and content \"([^\"]*)\" on Dropbox$")
    public void uploadFile(String filePath, String content) throws IOException, DbxException, TimeoutException, InterruptedException {
        dropBoxUtils.uploadFile(filePath, content);
    }


    @When("^.*checks? that file with path \"([^\"]*)\" exists? on Dropbox$")
    public void checkThatFileExists(String filePath) throws IOException, DbxException, TimeoutException, InterruptedException {
        Assertions.assertThat(dropBoxUtils.checkIfFileExists(filePath)).isTrue();
    }

    @When("^.*deletes? file with path \"([^\"]*)\" from Dropbox$")
    public void deleteFile(String filePath) throws DbxException {
        dropBoxUtils.deleteFile(filePath);
        Assertions.assertThat(dropBoxUtils.checkIfFileExists(filePath)).isFalse();
    }
}
