package io.syndesis.qe.steps.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.integrations.CiCdDialog;

import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CiCdDialogSteps {

    private CiCdDialog ciCdDialog = new CiCdDialog();

    @When("^create new tag with name (\\w+) in CI/CD dialog$")
    public void createNewTag(String tagName) {
        ciCdDialog.addNewTag(tagName);
    }

    @When("^rename tag (\\w+) to (\\w+) in CI/CD dialog$")
    public void renameTag(String tagName, String newName) {
        ciCdDialog.updateTag(tagName, newName);
    }

    @When("^uncheck tag (\\w+) in CI/CD dialog$")
    public void uncheck(String tagName) {
        ciCdDialog.uncheckTag(tagName);
    }

    @When("^check tag (\\w+) in CI/CD dialog$")
    public void checkTag(String tagName) {
        ciCdDialog.checkTag(tagName);
    }

    @When("^delete tag (\\w+) in CI/CD dialog$")
    public void deleteTag(String tagName) {
        ciCdDialog.removeTag(tagName);
    }

    @When("^save CI/CD dialog$")
    public void saveDialog() {
        ciCdDialog.saveDialog();
    }

    @When("^cancel CI/CD dialog$")
    public void cancelDialog() {
        ciCdDialog.cancelDialog();
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @Then("^check that CI/CD dialog contains tags$")
    public void checkAllTags(DataTable tagsData) {
        List<String> desiredTags = tagsData.cells().get(0);
        List<String> tags = ciCdDialog.getAllTags();
        assertThat(tags).hasSameElementsAs(desiredTags);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @Then("^check that only following tags are checked in CI/CD dialog$")
    public void checkCheckedTags(DataTable tagsData) {
        List<String> desiredTags = tagsData.cells().get(0);
        List<String> checkedTags = ciCdDialog.getOnlyCheckedTags();
        assertThat(checkedTags).hasSameElementsAs(desiredTags);
    }

    @Then("^check that tag (\\w+) doesn't exist in CI/CD dialog$")
    public void checkTagWasDeleted(String tagName) {
        assertThat(ciCdDialog.getAllTags()).doesNotContain(tagName);
    }

    @Then("^check that tag (\\w+) is not checked in CI/CD dialog$")
    public void checkTagIsNotChecked(String tagName) {
        assertThat(ciCdDialog.getOnlyCheckedTags()).doesNotContainSequence(tagName);
    }
}
