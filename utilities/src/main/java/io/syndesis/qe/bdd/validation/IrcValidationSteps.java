package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;

import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.TestUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrcValidationSteps {
    private IRCConnection connection;
    private IrcListener listener;

    @When("^connect IRC listener to channels \"([^\"]*)\"$")
    public void connectListener(String channels) throws Exception {
        connection = new IRCConnection(StringUtils.substringBetween(TestConfiguration.openShiftUrl(), "//", ":"), new int[] {31111},
                null, "checker", "checker", null);
        listener = new IrcListener();
        connection.addIRCEventListener(listener);
        connection.connect();
        Thread.sleep(15000L);
        for (String chan : channels.split(",")) {
            connection.doJoin(chan);
        }
    }

    @Then("^verify that the message with content \'([^\']*)\' was posted to channels \"([^\"]*)\"$")
    public void verifyThatMessageWasPosted(String content, String channels) {
        TestUtils.sleepIgnoreInterrupt(30000L);
        final int channelsCount = channels.split(",").length;
        assertThat(listener.getReceivedMessages().keySet()).size().isEqualTo(channelsCount);
        for (String channel : channels.split(",")) {
            assertThat(listener.getReceivedMessages().get(channel)).isEqualTo(content);
        }
        connection.close();
    }

    @When("^send message to IRC user \"([^\"]*)\" with content \'([^\']*)\'$")
    public void sendMessage(String target, String msg) {
        connection.doPrivmsg(target, msg);
    }

    public class IrcListener implements IRCEventListener {
        @Getter
        Map<String, String> receivedMessages = new HashMap<>();

        @Override
        public void onRegistered() {
        }

        @Override
        public void onDisconnected() {
        }

        @Override
        public void onError(String msg) {
        }

        @Override
        public void onError(int num, String msg) {
        }

        @Override
        public void onInvite(String chan, IRCUser u, String nickPass) {
        }

        @Override
        public void onJoin(String chan, IRCUser u) {
        }

        @Override
        public void onKick(String chan, IRCUser u, String nickPass, String msg) {
        }

        @Override
        public void onMode(IRCUser u, String nickPass, String mode) {
        }

        public void onMode(String chan, IRCUser u, IRCModeParser mp) {
        }

        @Override
        public void onNick(IRCUser u, String nickNew) {
        }

        @Override
        public void onNotice(String target, IRCUser u, String msg) {
        }

        @Override
        public void onPart(String chan, IRCUser u, String msg) {
        }

        @Override
        public void onPrivmsg(String chan, IRCUser u, String msg) {
            receivedMessages.put(chan, msg);
        }

        @Override
        public void onQuit(IRCUser u, String msg) {
        }

        @Override
        public void onReply(int num, String value, String msg) {
        }

        @Override
        public void onTopic(String chan, IRCUser u, String topic) {
        }

        @Override
        public void onPing(String p) {
        }

        @Override
        public void unknown(String a, String b, String c, String d) {
        }
    }
}
