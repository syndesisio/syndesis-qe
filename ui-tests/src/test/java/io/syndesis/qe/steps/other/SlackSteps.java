package io.syndesis.qe.steps.other;

import com.github.seratch.jslack.api.methods.SlackApiException;
import io.cucumber.java.en.When;
import io.syndesis.qe.utils.SlackConnector;
import io.syndesis.qe.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

public class SlackSteps {
    @Lazy
    @Autowired
    private SlackConnector slack;

    @When("^.*checks? that last slack message equals \"([^\"]*)\" on channel \"([^\"]*)\"$")
    public void checkMessage(String message, String channel) throws SlackApiException, IOException {
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        slack.checkLastMessageFromChannel(message, channel);
    }

    @When("^.*send? message \"([^\"]*)\" on channel \"([^\"]*)\"$")
    public void sendMessage(String message, String channel) throws InterruptedException, SlackApiException, IOException {
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        slack.sendMessage(message, channel);
    }
}
