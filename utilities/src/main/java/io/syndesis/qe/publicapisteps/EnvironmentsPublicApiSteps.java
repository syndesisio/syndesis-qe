package io.syndesis.qe.publicapisteps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.endpoints.publicendpoint.EnvironmentsPublicEndpoint;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvironmentsPublicApiSteps {

    @Autowired
    private EnvironmentsPublicEndpoint environmentsEndpoint;

    @Then("^check that Syndesis doesn't contain any tag$")
    public void checkThatSyndesisIsClear() {
        assertThat(environmentsEndpoint.getAllEnvironments().size()).isEqualTo(0);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @Then("^check that Syndesis contains exactly tags$")
    public void checkAllTags(DataTable tagsData) {
        List<String> desiredTags = tagsData.cells().get(0);
        assertThat(environmentsEndpoint.getAllEnvironments())
            .hasSameSizeAs(desiredTags)
            .containsAll(desiredTags);
    }

    @Then("^check that tag (\\w+) is used in (\\d+) integrations$")
    public void checkAllTags(String tag, int numberOfUsag) {
        Optional<Map<String, String>> particularTag = environmentsEndpoint.getAllEnvironmentsWithUses().stream()
            .filter(map -> map.get("name").equalsIgnoreCase(tag)).findFirst();
        assertThat(particularTag.get()).containsEntry("uses", String.valueOf(numberOfUsag));
    }

    @Then("^check that tag with name (\\w+) is in the tag list$")
    public void checkTagExist(String tagName) {
        assertThat(environmentsEndpoint.getAllEnvironments()).contains(tagName);
    }

    @Then("^check that tag with name (\\w+) is not in the tag list$")
    public void checkTagNotExist(String tagName) {
        assertThat(environmentsEndpoint.getAllEnvironments()).doesNotContain(tagName);
    }

    @When("^update tag with name (\\w+) to (\\w+)$")
    public void updateTag(String tagName, String newTagName) {
        environmentsEndpoint.renameEnvironment(tagName, newTagName);
    }

    @When("^delete tag with name (\\w+)$")
    public void deleteTag(String tagName) {
        environmentsEndpoint.deleteEnvironment(tagName);
    }

    @When("^delete all tags in Syndesis$")
    public void deleteAllTags() {
        for (String tag : environmentsEndpoint.getAllEnvironments()) {
            deleteTag(tag);
        }
    }

    @When("^add tag (\\w+) to Syndesis$")
    public void addTag(String tagName) {
        environmentsEndpoint.addNewEnvironment(tagName);
    }
}
