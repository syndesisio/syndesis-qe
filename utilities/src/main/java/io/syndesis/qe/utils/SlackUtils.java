package io.syndesis.qe.utils;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import org.assertj.core.api.Assertions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Lazy
public class SlackUtils {
    private static final Slack SLACK = Slack.getInstance();
    private static String token;

    /**
     * To create instance of slack connector, correct credentials must be set up. Class expects
     * slack connector with name "QE Slack". Syndesis connector requires "webhookUrl" with
     * web hook for desired workspace. For tests to work there must be property "Token". Token
     * is API Token for slack workspace custom integration called "Bots".
     */
    public SlackUtils() {
        Optional<Account> accountInfo = AccountsDirectory.getInstance().getAccount(Account.Name.SLACK);

        if (accountInfo.isPresent()) {
            SlackUtils.token = accountInfo.get().getProperties().get("token");
        } else {
            throw new InvalidParameterException("Credentials for QE Slack connector not found!");
        }
        Assertions.assertThat(SlackUtils.token).as("There is no slack property \"Token\" in credentials file!")
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
    public ChatPostMessageResponse sendMessage(String message, String channelName) throws IOException, SlackApiException {

        // find all channels in the workspace
        ChannelsListResponse channelsResponse = SLACK.methods().channelsList(ChannelsListRequest.builder().token(token).build());
        // find channelName
        Channel chann = channelsResponse.getChannels().stream()
                .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // https://slack.com/api/chat.postMessage
        ChatPostMessageResponse postResponse = SLACK.methods().chatPostMessage(ChatPostMessageRequest.builder()
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
        ChannelsListResponse channelsResponse = SLACK.methods().channelsList(ChannelsListRequest.builder().token(token).build());
        Assertions.assertThat(channelsResponse.isOk()).isTrue();
        // find channelName
        Channel chann = channelsResponse.getChannels().stream()
                .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // fetch history
        ChannelsHistoryResponse history = SLACK.methods().channelsHistory(ChannelsHistoryRequest.builder()
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
