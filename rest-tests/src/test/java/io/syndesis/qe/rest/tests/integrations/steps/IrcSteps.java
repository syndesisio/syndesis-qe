package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class IrcSteps {
    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    private Connector ircConnector;
    private Connection ircConnection;
    private Action connectorAction;
    private Map<String, String> properties;

    private void init(String action, String nickname, String channels) {
        ircConnector = connectorsEndpoint.get(RestTestsUtils.Connector.IRC.getId());
        ircConnection = connectionsEndpoint.get(RestTestsUtils.Connection.IRC.getId());
        connectorAction = TestUtils.findConnectorAction(ircConnector, action);
        properties = TestUtils.map(
                "nickname", nickname,
                "channels", channels
        );
        final Step ircStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .id(UUID.randomUUID().toString())
                .connection(ircConnection)
                .action(connectorAction)
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(ircStep));
    }

    @Given("^create IRC publish step with nickname \"([^\"]*)\" and channels \"([^\"]*)\"$")
    public void createIRCPublishStep(String nickname, String channels) {
        init("sendmsg", nickname, channels);
    }

    @Given("^create IRC subscribe step with nickname \"([^\"]*)\" and channels \"([^\"]*)\"$")
    public void createIRCSubscribeStep(String destinationType, String destinationName) {
        init("privmsg", destinationType, destinationName);
    }
}
