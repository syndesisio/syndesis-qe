package io.syndesis.qe.rest.tests.integrations.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.ExtensionsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

public class IntegrationSteps extends AbstractStep {
    @Autowired
    private StepsStorage steps;

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    @Autowired
    private ExtensionsEndpoint extensionsEndpoint;

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("^add \"([^\"]*)\" endpoint with connector id \"([^\"]*)\" and \"([^\"]*)\" action and with properties:$")
    public void createEndpointStepWithAndWith(String id, String connectorId, String action, DataTable properties) {
        final Connector connector = connectorsEndpoint.get(connectorId);
        final Connection connection = connectionsEndpoint.get(id);
        final Action ac = TestUtils.findConnectorAction(connector, action);
        final Map<String, String> stepProperties = new HashMap<>();

        List<Map<String, String>> maps = properties.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (Map.Entry<String,String> field : source.entrySet()) {
                stepProperties.put(field.getKey(), field.getValue());
            }

            final Step customStep = new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .id(UUID.randomUUID().toString())
                    .connection(connection)
                    .action(ac)
                    .configuredProperties(stepProperties)
                    .build();

            steps.getStepDefinitions().add(new StepDefinition(customStep));
        }
    }

    @When("^add \"([^\"]*)\" extension step with \"([^\"]*)\" action with properties:$")
    public void addExtensionIdWith(String name, String actionId, DataTable properties) {
        final Map<String, String> stepProperties = new HashMap<>();
        List<Map<String, String>> maps = properties.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (Map.Entry<String,String> field : source.entrySet()) {
                stepProperties.put(field.getKey(), field.getValue());
            }
        }

        Optional<Extension> e = extensionsEndpoint.list().stream().filter(ex -> ex.getName().equals(name)).findFirst();
        assertThat(e).isPresent();

        final Optional<Action> action = e.get().getActions().stream().filter(act -> act.getId().get().equals(actionId)).findFirst();
        assertThat(action).isPresent();

        final Step customStep = new Step.Builder()
                .stepKind(StepKind.extension)
                .name(name)
                .extension(e.get())
                .action(action.get())
                .configuredProperties(stepProperties)
                .id(UUID.randomUUID().toString())
                .build();
        steps.getStepDefinitions().add(new StepDefinition(customStep));
    }

    @When("^add advanced filter step with \"([^\"]*)\" expression$")
    public void addAdvancedFilterStepWithExpression(String expression) {
        final Step filter = new Step.Builder()
                .stepKind(StepKind.expressionFilter)
                .configuredProperties(TestUtils.map("filter", expression
                ))
                .name("Advanced filter")
                .id(UUID.randomUUID().toString())
                .build();
        steps.getStepDefinitions().add(new StepDefinition(filter));
    }

    @Given("^import extensions from syndesis-extensions folder$")
    public void importExtensionsFromSyndesisExtensionsFolder(DataTable properties) {
        List<String> extensions = properties.asList(String.class);
        for (String ext : extensions) {
            String defaultPath = "../syndesis-extensions/" + ext + "/target/";
            File[] files = new File(defaultPath).listFiles((dir, name) -> !name.contains("original") && name.endsWith(".jar"));
            assertThat(files).hasSize(1).doesNotContainNull();
            Extension e = extensionsEndpoint.uploadExtension(files[0]);
            extensionsEndpoint.installExtension(e);
        }
    }

    @Given("^change datashape of previous step to \"([^\"]*)\" direction, \"([^\"]*)\" type with specification \'([^\']*)\'$")
    public void changeDatashapeTo(String direction, String type, String specification) {
        Step lastStep = steps.getLastStepDefinition().getStep();
        Step withDatashape = new Step.Builder().createFrom(lastStep).action(
                withCustomDatashape(
                        lastStep.getAction().get(),
                        getConnectorDescriptor(
                                lastStep.getAction().get(), lastStep.getConfiguredProperties(), lastStep.getConnection().get().getId().get()
                        ),
                        direction,
                        DataShapeKinds.valueOf(type),
                        specification
                )
        ).build();

        steps.getStepDefinitions().remove(steps.getStepDefinitions().size() - 1);
        steps.getStepDefinitions().add(new StepDefinition(withDatashape));
    }

    @When("^rebuild integration with name \"([^\"]*)\"$")
    public void rebuildIntegration(String name) {
        Optional<String> integrationId = integrationsEndpoint.getIntegrationId(name);
        if (!integrationId.isPresent()) {
            fail("Unable to find ID for integration " + name);
        }

        integrationsEndpoint.activateIntegration(integrationId.get());
        final int maxRetries = 10;
        int retries = 0;
        boolean buildPodPresent = false;
        while (!buildPodPresent && retries < maxRetries) {
            buildPodPresent = OpenShiftUtils.client().pods().list().getItems().stream().anyMatch(
                    p -> p.getMetadata().getName().contains(name.toLowerCase().replaceAll(" ", "-"))
                            && p.getMetadata().getName().endsWith("-build"));
            TestUtils.sleepIgnoreInterrupt(10000L);
            retries++;
        }
    }
}
