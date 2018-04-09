package io.syndesis.qe.bdd;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cucumber.api.java.en.Given;
import io.atlasmap.v2.MappingType;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.datamapper.AtlasMapperGenerator;
import io.syndesis.qe.bdd.entities.DataMapperDefinition;
import io.syndesis.qe.bdd.entities.DataMapperStepDefinition;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.IntegrationOverviewEndpoint;
import io.syndesis.qe.endpoints.IntegrationsDeploymentEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.model.IntegrationOverview;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UiComplexSteps extends AbstractStep {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private IntegrationOverviewEndpoint integrationOverviewEndpoint;
    @Autowired
    private AtlasMapperGenerator atlasGenerator;

    @Given("^db to db \"([^\"]*)\" integration with period (\\d+) ms$")
    public void dbToDbIntegrationWithPeriodMs(String integrationName, int ms) throws IOException {

        final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
        final Connector dbConnector = connectorsEndpoint.get("sql");

        final String sqlStartQuery = "SELECT * FROM CONTACT";
        final String sqlFinishQuery = "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)";
        final String datamapperTemplate = "db-db.json";

//1.        @Then("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms")
        final Action dbAction1 = TestUtils.findConnectorAction(dbConnector, "sql-start-connector");
        final Map<String, String> properties1 = TestUtils.map("query", sqlStartQuery, "schedulerPeriod", ms);
        final ConnectorDescriptor connectorDescriptor1 = getConnectorDescriptor(dbAction1, properties1, dbConnection.getId().get());

        //to be reported: period is not part of .json step (when checked via browser).
        final Step dbStep1 = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(dbAction1)
                .configuredProperties(properties1)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep1, connectorDescriptor1));

//2.A @And("start mapper definition with name: \"([^\"]*)\"")
        String mapperName = "mapping 1";
        final Step mapperStep = new Step.Builder()
                .stepKind(StepKind.mapper)
                .name(mapperName)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(mapperStep, new DataMapperDefinition()));

//2.B @Then("MAP using Step (\\d+) and field \"([^\"]*)\" to \"([^\"]*)\"")
        int fromStep = 1;
        String fromField = "first_name";
        String toField = "TASK";

        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(fromField));
        newDmStep.setOutputFields(Arrays.asList(toField));
        newDmStep.setMappingType(MappingType.MAP);
        newDmStep.setStrategy(null);
        steps.getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);

//3.        @Then("^create finish DB invoke sql action step with query \"([^\"]*)\"")
        final Action dbAction2 = TestUtils.findConnectorAction(dbConnector, "sql-connector");
        final Map<String, String> properties2 = TestUtils.map("query", sqlFinishQuery);
        final ConnectorDescriptor connectorDescriptor2 = getConnectorDescriptor(dbAction2, properties2, dbConnection.getId().get());

        final Step dbStep2 = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(dbConnection)
                .action(dbAction2)
                .configuredProperties(properties2)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(dbStep2, connectorDescriptor2));

//4.    @When("^create integration with name: \"([^\"]*)\"")
        processMapperSteps();

        Integration integration = new Integration.Builder()
                .steps(steps.getSteps())
                .name(integrationName)
                .description("Awkward UI integration.")
                .build();

        log.info("Creating integration {}", integration.getName());
        String integrationId = integrationsEndpoint.create(integration).getId().get();
        log.info("Publish integration with ID: {}", integrationId);
        publishIntegration(integrationId);

        log.debug("Flushing used steps");
        steps.flushStepDefinitions();

//5.        @Then("^wait for integration with name: \"([^\"]*)\" to become active")
        final List<Integration> integrations = integrationsEndpoint.list().stream()
                .filter(item -> item.getName().equals(integrationName))
                .collect(Collectors.toList());

        final long start = System.currentTimeMillis();
        //wait for activation
        log.info("Waiting until integration \"{}\" becomes active. This may take a while...", integrationName);

        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);

        final boolean activated = TestUtils.waitForPublishing(integrationOverviewEndpoint, integrationOverview, TimeUnit.MINUTES, 10);
        Assertions.assertThat(activated).isEqualTo(true);
        log.info("Integration pod has been started. It took {}s to build the integration.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
    }

    //    AUXILIARIES:
    private String getDbConnectionId() {
        return connectionsEndpoint.list().stream().filter(s -> "PostgresDB".equals(s.getName())).findFirst().get().getId().get();
    }

    private void processMapperSteps() {

        List<StepDefinition> mappers = steps.getStepDefinitions().stream().filter(
                s -> s.getStep().getStepKind().equals(StepKind.mapper)).collect(Collectors.toList());

        if (mappers.isEmpty()) {
            log.debug("There are no mappers in this integration, proceeding...");
        } else {
            //mapping can be done on steps that preceed mapper step and the single step, which follows the mapper step.
            log.info("Found mapper step, creating new atlas mapping.");
            for (int i = 0; i < mappers.size(); i++) {
                List<StepDefinition> precedingSteps = steps.getStepDefinitions().subList(0, steps.getStepDefinitions().indexOf(mappers.get(i)))
                        .stream().filter(s -> s.getConnectorDescriptor().isPresent()).collect(Collectors.toList());
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

    /**
     * Publish integration
     *
     * @param integrationId id of integration to be published
     */
    private void publishIntegration(String integrationId) {

        IntegrationsDeploymentEndpoint intDeployments = new IntegrationsDeploymentEndpoint(integrationId);
        intDeployments.activate();
    }
}
