package io.syndesis.qe.validation;

import io.syndesis.qe.util.DropBoxUtils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.cucumber.java.en.When;

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
        Assertions.assertThat(TestUtils.waitForEvent(r -> r, () -> dropBoxUtils.checkIfFileExists(filePath),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
    }

    @When("^delete file with path \"([^\"]*)\" from Dropbox$")
    public void deleteFile(String filePath) throws DbxException {
        try {
            dropBoxUtils.deleteFile(filePath);
        } catch (DeleteErrorException e) {
            if (!e.getMessage().contains("not_found")) {
                throw e;
            }
        }
        Assertions.assertThat(dropBoxUtils.checkIfFileExists(filePath)).isFalse();
    }
}
