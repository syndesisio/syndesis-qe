package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SalesforceSteps {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    public SalesforceSteps() {
    }

    @Given("^create SF \"([^\"]*)\" action step on field: \"([^\"]*)\"$")
    public void createSfStepWithAction(String action, String field) {
        final Connector salesforceConnector = connectorsEndpoint.get("salesforce");
        final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
        final Step salesforceStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(salesforceConnection)
                .action(TestUtils.findConnectorAction(salesforceConnector, "salesforce-on-" + action))
                .configuredProperties(TestUtils.map("sObjectName", field))
                .build();
        steps.getSteps().add(salesforceStep);
    }

    @Given("^create SF step with action: \"([^\"]*)\"")
    public void createSfActionStep(String action) {
        final Connector salesforceConnector = connectorsEndpoint.get("salesforce");

        final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
        final Step salesforceStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(salesforceConnection)
                .action(TestUtils.findConnectorAction(salesforceConnector, action))
                .build();
        steps.getSteps().add(salesforceStep);
    }
}
