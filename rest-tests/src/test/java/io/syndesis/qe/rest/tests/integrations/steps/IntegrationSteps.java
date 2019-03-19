package io.syndesis.qe.rest.tests.integrations.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ExtensionsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

public class IntegrationSteps extends AbstractStep {
    @Autowired
    private ExtensionsEndpoint extensionsEndpoint;

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("add \"([^\"]*)\" endpoint with connector id \"([^\"]*)\" and \"([^\"]*)\" action and with properties:")
    public void createConnectorStep(String id, String connectorId, String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, connectorId);
        super.addProperty(StepProperty.CONNECTION_ID, id);
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMaps(String.class, String.class).get(0));
        super.createStep();
    }

    @When("^add \"([^\"]*)\" extension step with \"([^\"]*)\" action with properties:$")
    public void addExtensionIdWith(String name, String actionId, DataTable properties) {
        Optional<Extension> e = extensionsEndpoint.list().stream().filter(ex -> ex.getName().equals(name)).findFirst();
        assertThat(e).isPresent();

        final Optional<Action> action = e.get().getActions().stream().filter(act -> act.getId().get().equals(actionId)).findFirst();
        assertThat(action).isPresent();

        super.addProperty(StepProperty.KIND, StepKind.extension);
        super.addProperty(StepProperty.ACTION, action.get());
        super.addProperty(StepProperty.PROPERTIES, properties.asMaps(String.class, String.class).get(0));
        super.addProperty(StepProperty.STEP_NAME, name);
        super.addProperty(StepProperty.EXTENSION, e.get());
        super.createStep();
    }

    @When("^add advanced filter step with \"([^\"]*)\" expression$")
    public void addAdvancedFilterStepWithExpression(String expression) {
        super.addProperty(StepProperty.KIND, StepKind.expressionFilter);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("filter", expression));
        super.addProperty(StepProperty.STEP_NAME, "Advanced filter");
        super.createStep();
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

    @Given("^change datashape of previous step to \"([^\"]*)\" direction, \"([^\"]*)\" type with specification \'([^\']*)\'$")
    public void changeDatashapeTo(String direction, String type, String specification) {
        Step lastStep = super.getSteps().getLastStepDefinition().getStep();
        Step withDatashape = new Step.Builder().createFrom(lastStep).action(
                super.withCustomDatashape(
                        lastStep.getAction().get(),
                        super.getConnectorDescriptor(
                                lastStep.getAction().get(), lastStep.getConfiguredProperties(), lastStep.getConnection().get().getId().get()
                        ),
                        direction,
                        DataShapeKinds.valueOf(type),
                        specification
                )
        ).build();

        StepsStorage steps = super.getSteps();
        steps.getStepDefinitions().remove(steps.getStepDefinitions().size() - 1);
        steps.getStepDefinitions().add(new StepDefinition(withDatashape));
    }
}
