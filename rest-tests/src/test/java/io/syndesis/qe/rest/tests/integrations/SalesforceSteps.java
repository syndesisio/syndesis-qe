package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SalesforceSteps extends AbstractStep {

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
        final Action sfAction = TestUtils.findConnectorAction(salesforceConnector, action);
        final Map<String, String> properties = TestUtils.map("sObjectName", field);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(sfAction, properties, RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
        final Step salesforceStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(salesforceConnection)
                .action(sfAction)
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(salesforceStep, connectorDescriptor));
    }
}
