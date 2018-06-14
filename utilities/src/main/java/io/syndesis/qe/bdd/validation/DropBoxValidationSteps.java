package io.syndesis.qe.bdd.validation;

import com.dropbox.core.DbxException;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.DropBoxUtils;
import io.syndesis.qe.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DropBoxValidationSteps {
    @Lazy
    @Autowired
    private DropBoxUtils dropBoxUtils;

    @When("upload file with path \"([^\"]*)\" and content \"([^\"]*)\" on Dropbox$")
    public void uploadFile(String filePath, String content) throws IOException, DbxException, TimeoutException, InterruptedException {
        dropBoxUtils.uploadFile(filePath, content);
    }


    @When("check that file with path \"([^\"]*)\" exists on Dropbox$")
    public void checkThatFileExists(String filePath) throws IOException, DbxException, TimeoutException, InterruptedException {
        boolean fileExists = TestUtils.waitForEvent(r -> r, () -> dropBoxUtils.checkIfFileExists(filePath), TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15);
        Assertions.assertThat(fileExists).isTrue();
    }

    @When("delete file with path \"([^\"]*)\" from Dropbox$")
    public void deleteFile(String filePath) throws DbxException {
        dropBoxUtils.deleteFile(filePath);
        Assertions.assertThat(dropBoxUtils.checkIfFileExists(filePath)).isFalse();
    }
}
