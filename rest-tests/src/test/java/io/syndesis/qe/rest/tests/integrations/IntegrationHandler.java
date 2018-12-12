package io.syndesis.qe.rest.tests.integrations;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.datamapper.AtlasMapperGenerator;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
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
    private AtlasMapperGenerator atlasGenerator;

    public IntegrationHandler() {
    }

    @When("^create integration with name: \"([^\"]*)\"")
    public void createActiveIntegrationFromGivenSteps(String integrationName) {
        createIntegrationFromGivenStepsWithState(integrationName, "Published");
    }

    @When("^create new integration with name: \"([^\"]*)\" and desiredState: \"([^\"]*)\"")
    public void createIntegrationFromGivenStepsWithState(String integrationName, String desiredState) {

        processMapperSteps();
        Integration integration = new Integration.Builder()
                    .name(integrationName)
                    .description("Awkward integration.")
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
            for (int i = 0; i < mappers.size(); i++) {
                // Get only those that have some action defined
                List<StepDefinition> precedingSteps = steps.getStepDefinitions().subList(0, steps.getStepDefinitions().indexOf(mappers.get(i)))
                        .stream().filter(s -> s.getStep().getAction().isPresent()).collect(Collectors.toList());
                StepDefinition followingStep = steps.getStepDefinitions().get(steps.getStepDefinitions().indexOf(mappers.get(i)) + 1);
                if (mappers.get(i).getStep().getConfiguredProperties().containsKey("atlasmapping")) {
                    //TODO(tplevko): think of some way to substitute placeholders for the step ID's
                    reflectStepIdsInAtlasMapping(mappers.get(i), precedingSteps, followingStep);
                } else {
                    //TODO(tplevko): fix for more than one preceding step.
                    mappers.get(i).setStep(atlasGenerator.getAtlasMappingStep(mappers.get(i), precedingSteps, followingStep));
                }
            }
        }
    }

    private void reflectStepIdsInAtlasMapping(StepDefinition mapping, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
