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
 * Steps for Twitter mention to Salesforce upsert contact integration.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwSteps {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;
    public static final String SYNDESIS_TALKY_ACCOUNT = "twitter_talky";

    public TwSteps() {
    }

    @Given("^create TW mention step with \"([^\"]*)\" action")
    public void createTwitterStep(String twitterAction) {

        final Connector twitterConnector = connectorsEndpoint.get("twitter");
        final Connection twitterConnection = connectionsEndpoint.get(RestConstants.getInstance().getTWITTER_CONNECTION_ID());
        final Step twitterStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(twitterConnection)
                .action(TestUtils.findConnectorAction(twitterConnector, twitterAction))
                .build();
        steps.getSteps().add(twitterStep);
    }
}
