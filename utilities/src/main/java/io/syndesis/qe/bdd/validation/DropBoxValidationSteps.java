package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.dropbox.core.DbxException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.When;
import io.syndesis.qe.utils.DropBoxUtils;
import io.syndesis.qe.utils.TestUtils;

public class DropBoxValidationSteps {
    @Lazy
    @Autowired
    private DropBoxUtils dropBoxUtils;

    @When("^upload file with path \"([^\"]*)\" and content \"([^\"]*)\" on Dropbox$")
    public void uploadFile(String filePath, String content) throws IOException, DbxException {
        dropBoxUtils.uploadFile(filePath, content);
    }


    @When("^check that file with path \"([^\"]*)\" exists on Dropbox$")
    public void checkThatFileExists(String filePath) {
        assertThat(TestUtils.waitForEvent(r -> r, () -> dropBoxUtils.checkIfFileExists(filePath),
                TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
    }

    @When("^delete file with path \"([^\"]*)\" from Dropbox$")
    public void deleteFile(String filePath) throws DbxException {
        dropBoxUtils.deleteFile(filePath);
        assertThat(dropBoxUtils.checkIfFileExists(filePath)).isFalse();
    }
}
