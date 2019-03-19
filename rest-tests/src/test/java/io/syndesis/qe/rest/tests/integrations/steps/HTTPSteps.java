package io.syndesis.qe.rest.tests.integrations.steps;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class HTTPSteps extends AbstractStep {
    private void createStep(String method, String path, long period, String timeunit) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.HTTP.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.HTTP.getId());
        super.addProperty(StepProperty.ACTION, period == -1 ? "http4-invoke-url" : "http4-periodic-invoke-url");
        final Map<String, String> properties = TestUtils.map(
                "path", path,
                "httpMethod", method
        );

        if (period != -1) {
            properties.put("schedulerExpression", TimeUnit.MILLISECONDS.convert(period, TimeUnit.valueOf(timeunit)) + "");
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @Given("^create HTTP \"([^\"]*)\" step with period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriod(String method, long period, String timeunit) {
        createStep(method, "/", period, timeunit);
    }

    @Given("^create HTTP \"([^\"]*)\" step with path \"([^\"]*)\" and period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriodAndPath(String method, String path, long period, String timeunit) {
        createStep(method, path, period, timeunit);
    }

    @Given("^create HTTP \"([^\"]*)\" step$")
    public void createHTTPStep(String method) {
        createStep(method, "/", -1, null);
    }
}
