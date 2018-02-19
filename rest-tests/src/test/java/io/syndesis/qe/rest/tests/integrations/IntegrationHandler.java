package io.syndesis.qe.rest.tests.integrations;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.model.integration.Integration;
import io.syndesis.qe.endpoints.IntegrationsDeploymentEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * Used for generation of integrations using the steps in StepsStorage bean.
 *
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class IntegrationHandler {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    public IntegrationHandler() {
    }

    @When("^create integration with name: \"([^\"]*)\"")
    public void createActiveIntegrationFromGivenSteps(String integrationName) {
        createIntegrationFromGivenStepsWithState(integrationName, "Published");
    }

    @When("^create new integration with name: \"([^\"]*)\" and desiredState: \"([^\"]*)\"")
    public void createIntegrationFromGivenStepsWithState(String integrationName, String desiredState) {
        Integration integration = new Integration.Builder()
                .steps(steps.getSteps())
                .name(integrationName)
                .description("Awkward integration.")
                .build();

        log.info("Creating integration {}", integration.getName());
        String integrationId = integrationsEndpoint.create(integration).getId().get();
        log.info("Publish integration with ID: {}", integrationId);
        if (desiredState.contentEquals("Published")) {
            publishIntegration(integrationId);
        }

        //after the integration is created - the steps are cleaned for further use.
        log.debug("Flushing used steps");
        steps.flushSteps();
    }

    @When("^set integration with name: \"([^\"]*)\" to desiredState: \"([^\"]*)\"")
    public void changeIntegrationState(String integrationName, String desiredState) {

        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        log.info("Updating integration \"{}\" to state \"{}\"", integrationName, desiredState);
        if (desiredState.contentEquals("Published")) {
            publishIntegration(integrationId);
        }
        if (desiredState.contentEquals("Unpublished")) {
            unpublishIntegration(integrationId);
        }
    }

    @Then("^try to create new integration with the same name: \"([^\"]*)\" and state: \"([^\"]*)\"$")
    public void sameNameIntegrationValidation(String integrationName, String desiredState) {

        final Integration integration = new Integration.Builder()
                .steps(steps.getSteps())
                .name(integrationName)
                .description("Awkward integration.")
                .build();

        log.info("Creating integration {}", integration.getName());
        Assertions.assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> {
                    integrationsEndpoint.create(integration);
                })
                .withMessageContaining("HTTP 400 Bad Request")
                .withNoCause();
        log.debug("Flushing used steps");
        steps.flushSteps();
    }

    /**
     * Publish integration
     *
     * @param integrationId id of integration to be published
     */
    private void publishIntegration(String integrationId) {

        IntegrationsDeploymentEndpoint intDeployments = new IntegrationsDeploymentEndpoint(integrationId);
        intDeployments.activate();
    }

    /**
     * Unublish Integration
     *
     * @param integrationId id of integration to be unpublished
     */
    private void unpublishIntegration(String integrationId) {
        IntegrationsDeploymentEndpoint intDeployments = new IntegrationsDeploymentEndpoint(integrationId);
        intDeployments.deactivate(1);
    }
}
