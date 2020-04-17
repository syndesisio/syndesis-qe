package io.syndesis.qe.steps.integrations.CiCd;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.integrations.CiCd.ManageCICDPage;

import com.codeborne.selenide.Condition;

import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;

public class ManageCiCdPageSteps {

    private ManageCICDPage ciCdPage = new ManageCICDPage();

    @When("^create new tag with name (\\w+) in Manage CI/CD page$$")
    public void createNewTag(String tagName) {
        ciCdPage.clickOnAddNewTagButton();
        ModalDialogPage addDialog = new ModalDialogPage();
        assertThat(addDialog.getTitleText()).isEqualTo("Add Tag Name");
        addDialog.fillInputByDataTestid("cicd-edit-dialog-tag-name", tagName);
        new ModalDialogPage().getButton("Save").click();
    }

    @When("^rename tag (\\w+) to (\\w+) in Manage CI/CD page$")
    public void renameTag(String tagName, String newName) {
        ciCdPage.clickOnEditButton(tagName);
        ModalDialogPage editDialog = new ModalDialogPage();
        assertThat(editDialog.getTitleText()).isEqualTo("Edit Tag");
        editDialog.fillInputByDataTestid("cicd-edit-dialog-tag-name", newName);
        new ModalDialogPage().getButton("Save").click();
    }

    @When("^delete tag (\\w+) from Manage CI/CD page$")
    public void deleteTag(String tagName) {
        ciCdPage.clickOnRemoveButton(tagName);
        ModalDialogPage warning = new ModalDialogPage();
        assertThat(warning.getTitleText()).isEqualTo("Are you sure you want to remove the tag?");
        new ModalDialogPage().getButton("Yes").click();
    }

    @Then("^check that tag (\\w+) exist in Manage CI/CD page")
    public void checkTagExist(String tagName) {
        assertThat(ciCdPage.getAllTags()).contains(tagName);
    }

    @Then("^check that tag (\\w+) doesn't exist in Manage CI/CD page")
    public void checkTagWasDeleted(String tagName) {
        assertThat(ciCdPage.getAllTags()).doesNotContain(tagName);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @Then("^check that Manage CI/CD page contains tags$")
    public void checkAllTags(DataTable tagsData) {
        List<String> desiredTags = tagsData.cells().get(0);
        List<String> tags = ciCdPage.getAllTags();
        assertThat(tags).containsExactlyInAnyOrderElementsOf(desiredTags);
    }

    @Then("^check that tag (\\w+) cannot be created because another tag with same name exist$")
    public void checkCreationSameTag(String tagName) {
        ciCdPage.clickOnAddNewTagButton();
        ModalDialogPage addDialog = new ModalDialogPage();
        assertThat(addDialog.getTitleText()).isEqualTo("Add Tag Name");
        addDialog.fillInputByDataTestid("cicd-edit-dialog-tag-name", tagName);
        addDialog.getElementByClassName("help-block").shouldBe(Condition.visible)
            .shouldHave(Condition.text("That tag name is already in use."));
        addDialog.getButton("Save").shouldBe(Condition.disabled);
        new ModalDialogPage().getButton("Cancel").click();
    }

    @Then("^check that tag (\\w+) cannot be updated to (\\w+) because another tag with same name exist$")
    public void checkRenameToExistingTag(String tagName, String newTagName) {
        ciCdPage.clickOnEditButton(tagName);
        ModalDialogPage editDialog = new ModalDialogPage();
        assertThat(editDialog.getTitleText()).isEqualTo("Edit Tag");
        editDialog.fillInputByDataTestid("cicd-edit-dialog-tag-name", tagName);
        editDialog.getElementByClassName("help-block").shouldBe(Condition.visible)
            .shouldHave(Condition.text("That tag name is already in use."));
        editDialog.getButton("Save").shouldBe(Condition.disabled);
        new ModalDialogPage().getButton("Cancel").click();
    }

    @Then("^check in CI/CD page that tag (\\w+) is used in (\\d+) integrations")
    public void checkUsage(String tagName, int numberOfUsage) {
        assertThat(ciCdPage.getNumberOfUsage(tagName)).isEqualTo(numberOfUsage);
    }
}
