package io.syndesis.qe.validation.publicapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.qe.endpoint.IntegrationOverviewEndpoint;
import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.endpoint.model.IntegrationOverview;
import io.syndesis.qe.endpoints.IntegrationsPublicEndpoint;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class IntegrationsPublicApiSteps {

    @Autowired
    private IntegrationsPublicEndpoint integrationsEndpoint;

    @Autowired
    private IntegrationsEndpoint internalIntegrationsEndpoint;

    @Autowired
    private IntegrationOverviewEndpoint integrationOverviewEndpoint;

    @Then("^check that state of the integration (\\w+) is (\\w+)$")
    public void checkState(String integrationName, String desiredState) {
        if ("Unpublished".equals(desiredState)) {
            String integrationId = internalIntegrationsEndpoint.getIntegrationId(integrationName).get();
            final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);
            TestUtils.waitForUnpublishing(integrationOverviewEndpoint, integrationOverview, TimeUnit.MINUTES, 10);
        }

        assertThat(integrationsEndpoint.getStateOfIntegration(integrationName).get("currentState").asText()).isEqualTo(desiredState);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @When("^add tags to integration (\\w+)$")
    public void addTagToIntegration(String integrationName, DataTable tagsData) {
        List<String> originalTags = tagsData.cells().get(0);

        Map<String, ContinuousDeliveryEnvironment> tags = integrationsEndpoint.addTagsToIntegration(integrationName, originalTags);

        assertThat(tags.keySet())
            .containsAll(originalTags);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     * NOTE that uncheck existing tags on the integration
     */
    @When("^update tags on integration (\\w+)$")
    public void updateTagToIntegration(String integrationName, DataTable tagsData) {
        List<String> originalTags = tagsData.cells().get(0);

        Map<String, ContinuousDeliveryEnvironment> tags = integrationsEndpoint.updateTagsInIntegration(integrationName, tagsData.cells().get(0));

        assertThat(tags.keySet())
            .containsExactlyInAnyOrderElementsOf(originalTags);
    }

    /**
     * DataTable ->  | tag1 | tag2 | tag3 |
     */
    @Then("^check that integration (\\w+) contains exactly tags$")
    public void checkTagsInIntegration(String integrationName, DataTable tagsData) {
        List<String> desiredTags = tagsData.cells().get(0);
        Map<String, ContinuousDeliveryEnvironment> tags = integrationsEndpoint.getAllTagsInIntegration(integrationName);

        assertThat(tags.keySet())
            .hasSameSizeAs(desiredTags)
            .containsExactlyInAnyOrderElementsOf(desiredTags);
    }

    @Then("^check that integration (\\w+) doesn't contain any tag$")
    public void checkNoTags(String integrationName) {
        assertThat(integrationsEndpoint.getAllTagsInIntegration(integrationName)).isEmpty();
    }

    @Then("^check that integration (\\w+) doesn't contain tag (\\w+)$")
    public void checkDoesNotContains(String integrationName, String tag) {
        assertThat(integrationsEndpoint.getAllTagsInIntegration(integrationName)).doesNotContainKeys(tag);
    }

    @When("^delete tag (\\w+) from the integration (\\w+)$")
    public void deleteTagFromIntegration(String tag, String integrationName) {
        integrationsEndpoint.deleteTagInIntegration(integrationName, tag);
    }

    @When("^deploy integration (\\w+) via PublicApi$")
    public void deployIntegration(String integrationName) {
        integrationsEndpoint.deployIntegration(integrationName);
        TestUtils.sleepIgnoreInterrupt(10000);
    }

    @When("^stop integration (\\w+)$")
    public void stopIntegration(String integrationName) {
        integrationsEndpoint.stopIntegration(integrationName);
        TestUtils.sleepIgnoreInterrupt(10000);
    }

    @Then("^check that verion of the integration (\\w+) is (\\d+)$")
    public void checkIntegrationVersion(String integrationName, int version) {
        assertThat(internalIntegrationsEndpoint.getIntegrationByName(integrationName).getVersion()).isEqualTo(version);
    }

    @When("^export integrations with tag (\\w+) as \"([^\"]*)\"( and ignore timestamp)?$")
    public void exportIntegrations(String tag, String name, String ignore) {
        exportIntegrationsAccordingToTag(tag, name, ignore);
    }

    @Then("^verify that status code after export integrations with tag (\\w+) is (\\d+)( when timestamp is ignored)?$")
    public void exportIntegrations(String tag, int statusCode, String ignore) {
        assertThat(exportIntegrationsAccordingToTag(tag, null, ignore)).isEqualTo(statusCode);
    }

    private int exportIntegrationsAccordingToTag(String tag, String name, String ignoreTimestamp) {
        int status = 0;
        if (ignoreTimestamp == null) {
            status = integrationsEndpoint.exportIntegrationsAccordingToTag(tag, name, false);
        } else {
            status = integrationsEndpoint.exportIntegrationsAccordingToTag(tag, name, true);
        }
        return status;
    }

    /**
     * Export all integration and mark it with the tag
     */
    @When("^export integrations with tag (\\w+) and others as \"([^\"]*)\"$")
    public void exportAllIntegrations(String tag, String name) {
        integrationsEndpoint.exportAllIntegrations(tag, name);
    }

    @When("^import integrations with tag (\\w+) with name \"([^\"]*)\"$")
    public void importIntegrations(String tag, String name) {
        integrationsEndpoint.importIntegration(tag, name);
        TestUtils.sleepIgnoreInterrupt(5000);
    }

    @When("^delete integration with name (\\w+)$")
    public void deleteIntegration(String integrationName) {
        internalIntegrationsEndpoint.delete(internalIntegrationsEndpoint.getIntegrationId(integrationName).get());
    }
}
