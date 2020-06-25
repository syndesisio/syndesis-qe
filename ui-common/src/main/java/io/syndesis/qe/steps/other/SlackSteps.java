package io.syndesis.qe.steps.other;

import io.syndesis.qe.utils.SlackUtils;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.github.seratch.jslack.api.methods.SlackApiException;

import java.io.IOException;

import io.cucumber.java.en.When;

public class SlackSteps {
    @Lazy
    @Autowired
    private SlackUtils slack;

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
