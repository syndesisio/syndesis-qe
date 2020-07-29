package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.utils.TestUtils;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter integrations related validation steps.
 * <p>
 * Jan 18, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwValidationSteps {
    private final Twitter twitterTalky;
    private final Twitter twitterListener;
    private final AccountsDirectory accountsDirectory;

    public TwValidationSteps() {
        accountsDirectory = AccountsDirectory.getInstance();

        final Account twitterTalkyAccount = accountsDirectory.get(Account.Name.TWITTER_TALKY);
        //twitterTalky
        final TwitterFactory factoryTalk = new TwitterFactory(new ConfigurationBuilder()
            .setOAuthConsumerKey(twitterTalkyAccount.getProperty("consumerKey"))
            .setOAuthConsumerSecret(twitterTalkyAccount.getProperty("consumerSecret"))
            .setOAuthAccessToken(twitterTalkyAccount.getProperty("accessToken"))
            .setOAuthAccessTokenSecret(twitterTalkyAccount.getProperty("accessTokenSecret"))
            .build());
        this.twitterTalky = factoryTalk.getInstance();

        final Account twitterListenerAccount = accountsDirectory.get(Account.Name.TWITTER_LISTENER);
        final TwitterFactory factoryListen = new TwitterFactory(new ConfigurationBuilder()
            .setOAuthConsumerKey(twitterListenerAccount.getProperty("consumerKey"))
            .setOAuthConsumerSecret(twitterListenerAccount.getProperty("consumerSecret"))
            .setOAuthAccessToken(twitterListenerAccount.getProperty("accessToken"))
            .setOAuthAccessTokenSecret(twitterListenerAccount.getProperty("accessTokenSecret"))
            .build());
        this.twitterListener = factoryListen.getInstance();
    }

    @Given("clean all tweets in twitter_talky account")
    public void cleanupTwSf() throws TwitterException {
        deleteAllTweets(twitterTalky);
    }

    @When("tweet a message from twitter_talky to {string} with text {string}")
    public void sendTweet(String toAcc, String tweet) throws TwitterException {
        final String message = tweet + " @" + accountsDirectory.getAccount(toAcc).get().getProperty("screenName");
        log.info("Sending a tweet from {}, to {} with message: {}", accountsDirectory.getAccount(Account.Name.TWITTER_TALKY)
            .get().getProperty("screenName"), accountsDirectory.getAccount(toAcc).get().getProperty("screenName"), message);
        twitterTalky.updateStatus(message);
        log.info("Tweet submitted.");
    }

    @When("send direct message from twitter_talky to {string} with text {string}")
    public void sendDirectMessage(String toAcc, String message) throws TwitterException {
        Optional<Account> optionalAccount = accountsDirectory.getAccount(toAcc);
        if (optionalAccount.isPresent()) {
            String screenName = optionalAccount.get().getProperty("screenName");
            log.info("Sending twitterTalky DM to user {} with text {}", screenName, message);
            twitterTalky.sendDirectMessage(screenName, message);
        } else {
            fail("Could not find account {}", toAcc);
        }
    }

    @Then("check that account {string} has DM from user {string} with text {string}")
    public void checkDirectMessage(String toAccountName, String fromAccountName, String message) {
        Twitter toAccount = getCorrectAccount(toAccountName);
        long fromAccontId = getSenderIdForAccount(fromAccountName);
        TestUtils.waitFor(() -> checkDirectMessage(toAccount, fromAccontId, message),
            10, 60,
            "Could not find correct twitter DM");
    }

    public long getSenderIdForAccount(String account) {
        try {
            return getCorrectAccount(account).getId();
        } catch (TwitterException e) {
            fail("Twitter exception for account " + account + " was thrown. Exception:\n" + e.getMessage());
            return -1;
        }
    }

    private Twitter getCorrectAccount(String account) {
        if ("twittertalky".equals(account.toLowerCase().replaceAll(" ", ""))) {
            return this.twitterTalky;
        } else if ("twitterlistener".equals(account.toLowerCase().replaceAll(" ", ""))) {
            return this.twitterListener;
        } else {
            fail("Wrong twitter account: " + account);
            return null;
        }
    }

    private boolean checkDirectMessage(Twitter twitter, long senderId, String messageText) {
        boolean result = false;

        try {
            for (DirectMessage message : twitter.getDirectMessages(100)) {
                log.info("twitter DM from user {} with text {}", message.getSenderId(), message.getText());
                if (messageText.equalsIgnoreCase(message.getText()) &&
                    senderId == message.getSenderId()) {

                    result = true;
                }
            }
        } catch (TwitterException e) {
            log.debug("Exception while twitter interaction", e);
        }

        return result;
    }

    @When("delete all direct messages received by {string} with text {string}")
    public void deleteAllDMs(String account, String text) throws TwitterException {
        if ("twittertalky".equals(account.toLowerCase().replaceAll(" ", ""))) {
            deleteAllDMs(this.twitterTalky, text);
        }

        if ("twitterlistener".equals(account.toLowerCase().replaceAll(" ", ""))) {
            deleteAllDMs(this.twitterListener, text);
        }
    }

    private void deleteAllDMs(Twitter twitter, String text) throws TwitterException {
        DirectMessageList messageList = twitter.getDirectMessages(100);
        for (DirectMessage directMessage : messageList) {
            if (directMessage.getText().contains(text)) {
                twitter.destroyDirectMessage(directMessage.getId());
            }
        }
    }

    private void deleteAllTweets(Twitter twitter) throws TwitterException {
        final ResponseList<Status> userTimeline = twitter.timelines().getUserTimeline();
        log.info("Deleting all tweets of: " + twitter.getScreenName());

        userTimeline.stream().forEach(s -> {
            try {
                twitter.destroyStatus(s.getId());
            } catch (TwitterException e) {
                log.warn("Cannot destroy status: " + s.getId());
            }
        });
    }
}
