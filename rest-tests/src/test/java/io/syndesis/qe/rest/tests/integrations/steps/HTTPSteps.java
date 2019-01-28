package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class HTTPSteps extends AbstractStep {
    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    private void createStep(String method, String path, long period, String timeunit, String datashape) {
        final Connector httpConnector = connectorsEndpoint.get(RestTestsUtils.Connector.HTTP.getId());
        final Connection httpConnection = connectionsEndpoint.get(RestTestsUtils.Connection.HTTP.getId());
        final String action = period == -1 ? "http4-invoke-url" : "http4-periodic-invoke-url";
        Action httpAction = TestUtils.findConnectorAction(httpConnector, action);
        final Map<String, String> properties = TestUtils.map(
                "path", path,
                "httpMethod", method
        );

        if (period != -1) {
            properties.put("schedulerExpression", TimeUnit.MILLISECONDS.convert(period, TimeUnit.valueOf(timeunit)) + "");
        }

        if (datashape != null) {
            final ConnectorDescriptor cd = getConnectorDescriptor(httpAction, properties, RestTestsUtils.Connection.HTTP.getId());
            httpAction = withCustomDatashape(
                    httpAction,
                    cd,
                    "out",
                    DataShapeKinds.XML_INSTANCE,
                    datashape);
        }

        final Step httpStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(httpConnection)
                .action(httpAction)
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(httpStep));
    }

    @Given("^create HTTP \"([^\"]*)\" step with period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriod(String method, long period, String timeunit) {
        createStep(method, "/", period, timeunit, null);
    }

    @Given("^create HTTP \"([^\"]*)\" step with path \"([^\"]*)\" and period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriodAndPath(String method, String path, long period, String timeunit) {
        createStep(method, path, period, timeunit, null);
    }

    @Given("^create HTTP \"([^\"]*)\" step$")
    public void createHTTPStep(String method) {
        createStep(method, "/", -1, null, null);
    }
}
