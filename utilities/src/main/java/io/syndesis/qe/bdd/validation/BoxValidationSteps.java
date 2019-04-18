package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.apache.commons.io.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.box.sdk.BoxFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.BoxUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoxValidationSteps {
    @Autowired
    private BoxUtils boxUtils;

    @Given("^remove all files from Box$")
    public void clearBox() {
        boxUtils.clearBox();
    }

    @When("^upload file with name \"([^\"]*)\" and content \"([^\"]*)\" to Box$")
    public void uploadFile(String name, String content) {
        boxUtils.uploadFile(name, content);
    }

    @Then("^verify that file count in Box is \\d$")
    public void verifyFileCount(int count) {
        assertThat(count).isEqualTo(boxUtils.getFileCount());
    }

    @Then("^verify that file \"([^\"]*)\" with content \"([^\"]*)\" is present in Box$")
    public void verifyFileWithContentIsPresent(String filename, String content) {
        BoxFile f = boxUtils.getFile(filename);
        assertThat(f).isNotNull();

        final String localFileName = "/tmp/" + UUID.randomUUID().toString();

        try (FileOutputStream fos = new FileOutputStream(localFileName)) {
            f.download(fos);
            assertThat(FileUtils.readFileToString(new File(localFileName), "UTF-8").trim()).isEqualTo(content);
        } catch (IOException ex) {
            fail("Unable to process file from Box: ", ex);
        }
    }
}
