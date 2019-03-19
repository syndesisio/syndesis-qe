package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.Given;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Steps for Twitter mention to Salesforce upsert contact integration.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public class TwitterSteps extends AbstractStep {
   @Given("^create TW mention step with \"([^\"]*)\" action")
    public void createTwitterStep(String twitterAction) {
       super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.TWITTER.getId());
       super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.TWITTER.getId());
       super.addProperty(StepProperty.ACTION, twitterAction);
       super.createStep();
    }
}
