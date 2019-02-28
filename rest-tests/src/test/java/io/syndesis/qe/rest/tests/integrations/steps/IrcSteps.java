package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;

public class IrcSteps extends AbstractStep {
    @When("^create IRC \"([^\"]*)\" step with nickname \"([^\"]*)\" and channels \"([^\"]*)\"$")
    public void createIrcStep(String action, String nickname, String channels) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.IRC.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.IRC.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
                "nickname", nickname,
                "channels", channels
        ));
        super.createStep();
    }
}
