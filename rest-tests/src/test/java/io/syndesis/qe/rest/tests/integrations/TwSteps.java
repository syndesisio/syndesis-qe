package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.UUID;

import cucumber.api.java.en.Given;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Steps for Twitter mention to Salesforce upsert contact integration.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwSteps extends AbstractStep {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    @Given("^create TW mention step with \"([^\"]*)\" action")
    public void createTwitterStep(String twitterAction) {
        final Connector twitterConnector = connectorsEndpoint.get(RestTestsUtils.Connector.TWITTER.getId());
        final Connection twitterConnection = connectionsEndpoint.get(RestTestsUtils.Connection.TWITTER.getId());
        final Action twAction = TestUtils.findConnectorAction(twitterConnector, twitterAction);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(twAction, new HashMap<>(), RestTestsUtils.Connection.TWITTER.getId());

        final Step twitterStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(twitterConnection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(twAction, connectorDescriptor))
                .build();
        steps.getStepDefinitions().add(new StepDefinition(twitterStep, connectorDescriptor));
    }
}
