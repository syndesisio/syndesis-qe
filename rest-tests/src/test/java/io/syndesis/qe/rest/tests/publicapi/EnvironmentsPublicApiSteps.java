package io.syndesis.qe.rest.tests.publicapi;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.endpoints.publicendpoint.EnvironmentsPublicEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class EnvironmentsPublicApiSteps {

    @Autowired
    private EnvironmentsPublicEndpoint environmentsEndpoint;

    @Then("check that number of tags is (\\w+)$")
    public void checkNumberOfTags(int numberOfTags) {
        assertThat(environmentsEndpoint.getAllEnvironments().size()).isEqualTo(numberOfTags);
    }

    @Then("check that tag with name ([^\"]*) is in the tag list$")
    public void checkTagExist(String tagName) {
        assertThat(environmentsEndpoint.getAllEnvironments()).contains(tagName);
    }

    @Then("check that tag with name ([^\"]*) is not in the tag list$")
    public void checkTagNotExist(String tagName) {
        assertThat(environmentsEndpoint.getAllEnvironments()).doesNotContain(tagName);
    }

    @When("update tag with name ([^\"]*) to ([^\"]*)$")
    public void updateTag(String tagName, String newTagName) {
        environmentsEndpoint.renameEnvironment(tagName, newTagName);
    }

    @When("delete tag with name ([^\"]*)$")
    public void deleteTag(String tagName) {
        environmentsEndpoint.deleteEnvironment(tagName);
    }
}
