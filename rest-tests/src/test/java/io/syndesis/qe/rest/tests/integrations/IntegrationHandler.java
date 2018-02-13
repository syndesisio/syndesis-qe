package io.syndesis.qe.rest.tests.integrations;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;

import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeploymentState;
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

    private IntegrationsEndpoint integrationsEndpoint;

    public IntegrationHandler() {
        integrationsEndpoint = new IntegrationsEndpoint();
    }

    @When("^create integration with name: \"([^\"]*)\"")
    public void createActiveIntegrationFromGivenSteps(String integrationName) {
        createIntegrationFromGivenStepsWithState(integrationName, "Active");
    }

    @When("^create new integration with name: \"([^\"]*)\" and desiredState: \"([^\"]*)\"")
    public void createIntegrationFromGivenStepsWithState(String integrationName, String desiredState) {
        Integration integration = new Integration.Builder()
                .steps(steps.getSteps())
                .name(integrationName)
                .desiredStatus(IntegrationDeploymentState.valueOf(desiredState))
                .description("Awkward integration.")
                .build();

        log.info("Creating integration {}", integration.getName());
        integrationsEndpoint.create(integration);
        //after the integration is created - the steps are cleaned for further use.
        log.debug("Flushing used steps");
        steps.flushSteps();
    }

    @When("^set integration with name: \"([^\"]*)\" to desiredState: \"([^\"]*)\"")
    public void changeIntegrationState(String integrationName, String desiredState) {

        final List<Integration> integrations = integrationsEndpoint.list();
        final Integration integration = integrations.stream().filter(i -> i.getName().contentEquals(integrationName)).findFirst().get();
        final Integration updatedIngegration = new Integration.Builder().createFrom(integration)
                .desiredStatus(IntegrationDeploymentState.valueOf(desiredState))
                .build();

        log.info("Updating integration \"{}\" to state \"{}\"", integration.getName(), desiredState);
        integrationsEndpoint.update(integration.getId().get(), updatedIngegration);
    }

    @Then("^try to create new integration with the same name: \"([^\"]*)\" and state: \"([^\"]*)\"$")
    public void sameNameIntegrationValidation(String integrationName, String desiredState) {

        final Integration integration = new Integration.Builder()
                .steps(steps.getSteps())
                .name(integrationName)
                .desiredStatus(IntegrationDeploymentState.valueOf(desiredState))
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
}
