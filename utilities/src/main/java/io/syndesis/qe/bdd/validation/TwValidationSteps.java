package io.syndesis.qe.bdd.validation;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.RestConstants;
import lombok.extern.slf4j.Slf4j;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter integrations related validation steps.
 *
 * Jan 18, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwValidationSteps {

    private final Twitter twitter;
    private final AccountsDirectory accountsDirectory;

    public TwValidationSteps() {

        accountsDirectory = AccountsDirectory.getInstance();
        final Account twitterTalky = accountsDirectory.getAccount(RestConstants.SYNDESIS_TALKY_ACCOUNT).get();
        //twitter
        final TwitterFactory factory = new TwitterFactory(new ConfigurationBuilder()
                .setOAuthConsumerKey(twitterTalky.getProperty("consumerKey"))
                .setOAuthConsumerSecret(twitterTalky.getProperty("consumerSecret"))
                .setOAuthAccessToken(twitterTalky.getProperty("accessToken"))
                .setOAuthAccessTokenSecret(twitterTalky.getProperty("accessTokenSecret"))
                .build());
        twitter = factory.getInstance();
    }

    @Given("^clean all tweets in twitter_talky account")
    public void cleanupTwSf() throws TwitterException {
        deleteAllTweets(twitter);
    }

    @Then("^tweet a message from twitter_talky to \"([^\"]*)\" with text \"([^\"]*)\"")
    public void sendTweet(String toAcc, String tweet) throws TwitterException {
        final String message = tweet + " @" + accountsDirectory.getAccount(toAcc).get().getProperty("screenName");
        log.info("Sending a tweet from {}, to {} with message: {}", accountsDirectory.getAccount(RestConstants.SYNDESIS_TALKY_ACCOUNT)
                .get().getProperty("screenName"), accountsDirectory.getAccount(toAcc).get().getProperty("screenName"), message);
        twitter.updateStatus(message);
        log.info("Tweet submitted.");
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
