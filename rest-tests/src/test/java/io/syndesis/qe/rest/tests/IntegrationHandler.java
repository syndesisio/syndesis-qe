package io.syndesis.qe.rest.tests;

import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.datamapper.AtlasMapperGenerator;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.Verifier;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.server.openshift.Exposure;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

/**
 * Used for generation of integrations using the steps in StepsStorage bean.
 * <p>
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
    @Autowired
    private AtlasMapperGenerator amg;

    @When("^create integration with name: \"([^\"]*)\"")
    public void createActiveIntegrationFromGivenSteps(String integrationName) {
        createIntegrationFromGivenStepsWithState(integrationName, "Published");
    }

    @When("^create new integration with name: \"([^\"]*)\" and desiredState: \"([^\"]*)\"")
    public void createIntegrationFromGivenStepsWithState(String integrationName, String desiredState) {
        verifyConnections();
        processAggregateSteps();
        processMapperSteps();
        Set<String> tags = new HashSet<>();
        for (Step step : steps.getSteps()) {
            if (step.getConnection().isPresent()) {
                tags.addAll(step.getConnection().get().getTags());
            }
        }

        Integration integration = new Integration.Builder()
            .name(integrationName)
            .description("Awkward integration.")
            .tags(tags)
            .exposure(Exposure.SERVICE.toString())
            .addFlow(
                new Flow.Builder()
                    .id(UUID.randomUUID().toString())
                    .description(integrationName + "Flow")
                    .steps(steps.getSteps())
                    .build()
            )
            .build();

        log.info("Creating integration {}", integration.getName());
        String integrationId = integrationsEndpoint.create(integration).getId().get();
        log.info("Publish integration with ID: {}", integrationId);
        if (desiredState.contentEquals("Published")) {
            publishIntegration(integrationId);
        }

        //after the integration is created - the steps are cleaned for further use.
        log.debug("Flushing used steps");
        //TODO(tplevko): find some more elegant way to flush the steps before test start.
        steps.flushStepDefinitions();
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

    @When("delete integration with name {string}")
    public void deleteIntegration(String integrationName) {
        integrationsEndpoint.delete(integrationsEndpoint.getIntegrationId(integrationName).get());
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
                RestUtils.getClient()
                    .target(integrationsEndpoint.getEndpointUrl())
                    .request(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-User", "pista")
                    .header("X-Forwarded-Access-Token", "kral")
                    .header("SYNDESIS-XSRF-TOKEN", "awesome")
                    .post(Entity.entity(integration, MediaType.APPLICATION_JSON), JsonNode.class);
            })
            .withMessageContaining("HTTP 400 Bad Request")
            .withNoCause();
        log.debug("Flushing used steps");
        steps.flushStepDefinitions();
    }

    /**
     * Publish integration
     *
     * @param integrationId id of integration to be published
     */
    private void publishIntegration(String integrationId) {
        integrationsEndpoint.activateIntegration(integrationId);
    }

    /**
     * Unpublish Integration
     *
     * @param integrationId id of integration to be unpublished
     */
    private void unpublishIntegration(String integrationId) {
        int integrationVersion = integrationsEndpoint.get(integrationId).getVersion();
        log.info("Undeploying integration with integration version: {}", integrationVersion);
        integrationsEndpoint.deactivateIntegration(integrationId, integrationVersion);
    }

    /**
     * This should be updated for more than two steps, when it will work correctly in near future.
     */
    private void processMapperSteps() {
        List<StepDefinition> mappers = steps.getStepDefinitions().stream().filter(
            s -> s.getStep().getStepKind().equals(StepKind.mapper)).collect(Collectors.toList());
        if (mappers.isEmpty()) {
            log.debug("There are no mappers in this integration, proceeding...");
        } else {
            //mapping can be done on steps that preceed mapper step and the single step, which follows the mapper step.
            log.info("Found mapper step, creating new atlas mapping.");
            for (StepDefinition mapper : mappers) {
                List<StepDefinition> precedingSteps = steps.getStepDefinitions().subList(0, steps.getStepDefinitions().indexOf(mapper));
                StepDefinition followingStep = steps.getStepDefinitions().get(steps.getStepDefinitions().indexOf(mapper) + 1);
                if (mapper.getStep().getConfiguredProperties().containsKey("atlasmapping")) {
                    //TODO(tplevko): think of some way to substitute placeholders for the step ID's
                    reflectStepIdsInAtlasMapping(mapper, precedingSteps, followingStep);
                } else {
                    //TODO(tplevko): fix for more than one preceding step.
                    amg.setSteps(mapper, precedingSteps, followingStep);
                    mapper.setStep(amg.getAtlasMappingStep());
                }
            }
        }
    }

    /**
     * When there is a datamapper before the aggregate, it is needed to adopt the datashape of the step following the aggregate step.
     */
    private void processAggregateSteps() {
        for (int i = 0; i < steps.getStepDefinitions().size(); i++) {
            if (StepKind.aggregate == steps.getStepDefinitions().get(i).getStep().getStepKind()) {
                if (StepKind.mapper == steps.getStepDefinitions().get(i - 1).getStep().getStepKind()) {
                    StepDefinition stepDef = steps.getStepDefinitions().subList(i + 1, steps.getStepDefinitions().size())
                        .stream().filter(sd -> sd.getStep().getAction().isPresent()).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unable to find next step with an action defined"));

                    steps.getStepDefinitions().get(i).setStep(
                        steps.getStepDefinitions().get(i).getStep().updateInputDataShape(stepDef.getStep().getAction().get().getInputDataShape())
                    );
                    steps.getStepDefinitions().get(i).setStep(
                        steps.getStepDefinitions().get(i).getStep().updateOutputDataShape(stepDef.getStep().getAction().get().getOutputDataShape())
                    );
                }
            }
        }
    }

    private void reflectStepIdsInAtlasMapping(StepDefinition mapping, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Calls the verifier endpoint for all connections defined in the integration.
     */
    private void verifyConnections() {
        for (Step step : steps.getSteps()) {
            if (step.getStepKind() == StepKind.endpoint && step.getConnection().get().getConnector().get().getTags().contains("verifier")) {
                String response = Verifier.verify(step.getConnection().get().getConnectorId(), step.getConnection().get().getConfiguredProperties());
                log.debug(response);
                if (response.contains("ERROR") || response.contains("UNSUPPORTED")) {
                    throw new RuntimeException(String.format("Connection %s failed validation: %s", step.getConnection().get().getName(), response));
                }
            }
        }
    }
}
