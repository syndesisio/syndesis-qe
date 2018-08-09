package io.syndesis.qe.rest.tests.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
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

    @When("^add \"([^\"]*)\" endpoint with connector id \"([^\"]*)\" and \"([^\"]*)\" action and with properties:$")
    public void createEndpointStepWithAndWith(String id, String connectorId, String action, DataTable properties) {
        final Connector connector = connectorsEndpoint.get(connectorId);
        final Connection connection = connectionsEndpoint.get(id);
        final Action ac = TestUtils.findConnectorAction(connector, action);
        final Map<String, String> stepProperties = new HashMap<>();

        for (Map<String, String> source : properties.asMaps(String.class, String.class)) {
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
        for (Map<String, String> source : properties.asMaps(String.class, String.class)) {
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
}
