package io.syndesis.qe.utils;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsHistoryRequest;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsListRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsHistoryResponse;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Channel;
import com.github.seratch.jslack.api.model.Message;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Optional;

@Slf4j
@Component
public class SlackConnector {
    private static final Slack slack = Slack.getInstance();
    private static String token;

    /**
     * To create instance of slack connector, correct credentials must be set up. Class expects
     * slack connector with name "QE Slack". Syndesis connector requires "webhookUrl" with
     * web hook for desired workspace. For tests to work there must be property "Token". Token
     * is API Token for slack workspace custom integration called "Bots".
     */
    public SlackConnector() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("QE Slack");

        if (optional.isPresent()) {
            SlackConnector.token = optional.get().getProperties().get("Token");
        } else {
            throw new InvalidParameterException("Credentials for QE Slack connector not found!");
        }
        Assertions.assertThat(SlackConnector.token).as("There is no slack property \"Token\" in credentials file!")
                .isNotEmpty();

    }

    /**
     * Send message to specified channel.
     *
     * Note that Slack QE credentials must have "Token" property correctly set up for desired workspace.
     *
     * @param message
     * @param channelName
     * @return
     * @throws IOException
     * @throws SlackApiException
     * @throws InterruptedException
     */
    public ChatPostMessageResponse sendMessage(String message, String channelName) throws IOException, SlackApiException, InterruptedException {

        // find all channels in the workspace
        ChannelsListResponse channelsResponse = slack.methods().channelsList(ChannelsListRequest.builder().token(token).build());
        // find channelName
        Channel chann = channelsResponse.getChannels().stream()
                .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // https://slack.com/api/chat.postMessage
        ChatPostMessageResponse postResponse = slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                .token(token)
                .channel(chann.getId())
                .username("syndesis-bot")
                .text(message)
                .build());

        return postResponse;

    }


    /**
     * Check if last message on channel is expectedMessage.
     *
     * Note that Slack QE credentials must have "Token" property correctly set up for desired workspace.
     *
     * @param expectedMessage
     * @param channelName
     * @throws IOException
     * @throws SlackApiException
     */
    public void checkLastMessageFromChannel(String expectedMessage, String channelName) throws IOException, SlackApiException {

        // find all channels in the workspace
        ChannelsListResponse channelsResponse = slack.methods().channelsList(ChannelsListRequest.builder().token(token).build());
        Assertions.assertThat(channelsResponse.isOk()).isTrue();
        // find channelName
        Channel chann = channelsResponse.getChannels().stream()
                .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // fetch history
        ChannelsHistoryResponse history = slack.methods().channelsHistory(ChannelsHistoryRequest.builder()
                .token(token)
                .channel(chann.getId())
                .build());
        Assertions.assertThat(history.isOk()).isTrue();
        log.debug("Slack history fetched successfully...");

        // get last message
        Message lastMessage = history.getMessages().get(0);
        log.info("Last slack message in channel {} is '{}'", channelName, lastMessage.getText());
        Assertions.assertThat(lastMessage.getText()).isEqualToIgnoringCase(expectedMessage);
    }
}
