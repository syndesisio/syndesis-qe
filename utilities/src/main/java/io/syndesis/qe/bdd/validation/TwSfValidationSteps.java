package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.salesforce.Contact;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Validation steps for twitter to salesforce integration.
 *
 * Dec 8, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwSfValidationSteps {

	private final Twitter twitter;
	private final ForceApi salesforce;
	private final AccountsDirectory accountsDirectory;

	public TwSfValidationSteps() {

		accountsDirectory = AccountsDirectory.getInstance();
		final Account twitterTalky = accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT()).get();
		//twitter
		final TwitterFactory factory = new TwitterFactory(new ConfigurationBuilder()
				.setOAuthConsumerKey(twitterTalky.getProperty("consumerKey"))
				.setOAuthConsumerSecret(twitterTalky.getProperty("consumerSecret"))
				.setOAuthAccessToken(twitterTalky.getProperty("accessToken"))
				.setOAuthAccessTokenSecret(twitterTalky.getProperty("accessTokenSecret"))
				.build());
		twitter = factory.getInstance();
		// salesforce
		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();
		salesforce = new ForceApi(new ApiConfig()
				.setClientId(salesforceAccount.getProperty("clientId"))
				.setClientSecret(salesforceAccount.getProperty("clientSecret"))
				.setUsername(salesforceAccount.getProperty("userName"))
				.setPassword(salesforceAccount.getProperty("password"))
				.setForceURL(salesforceAccount.getProperty("loginUrl")));
	}

	@Given("^clean TW to SF scenario")
	public void cleanupTwSf() throws TwitterException {
		TestSupport.getInstance().resetDB();
		deleteSalesforceContact(salesforce, accountsDirectory.getAccount("twitter_talky").get().getProperty("screenName"));
		deleteAllTweets(twitter);
	}

	@Then("^tweet a message \"([^\"]*)\"")
	public void sendTweet(String tweet) throws TwitterException {
		Assertions.assertThat(getSalesforceContact(salesforce, accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT())
				.get().getProperty("screenName")).isPresent()).isEqualTo(false);
		final String message = tweet + " @" + accountsDirectory.getAccount("twitter_talky").get().getProperty("screenName");
		log.info("Sending a tweet from {}. Message: {}", accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT()).get().getProperty("screenName"), message);
		twitter.updateStatus(message);

		log.info("Tweet submitted.");
	}

	@Then("^validate record is present in SF \"([^\"]*)\"")
	public void validateIntegration(String record) throws TwitterException {
		Assertions.assertThat(getSalesforceContact(salesforce, accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT())
				.get().getProperty("screenName")).isPresent()).isEqualTo(false);

		log.info("Waiting until a contact appears in salesforce...");
		final long start = System.currentTimeMillis();
		final boolean contactCreated = TestUtils.waitForEvent(contact -> contact.isPresent(),
				() -> getSalesforceContact(salesforce, accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT()).get().getProperty("screenName")),
				TimeUnit.MINUTES,
				2,
				TimeUnit.SECONDS,
				5);
		Assertions.assertThat(contactCreated).as("Contact has appeard in salesforce").isEqualTo(true);
		log.info("Contact appeared in salesforce. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));

		final Contact createdContact = getSalesforceContact(salesforce, accountsDirectory.getAccount(RestConstants.getInstance().getSYNDESIS_TALKY_ACCOUNT()).get().getProperty("screenName")).get();
		Assertions.assertThat(createdContact.getDescription()).startsWith(record);
		Assertions.assertThat(createdContact.getFirstName()).isNotEmpty();
		Assertions.assertThat(createdContact.getLastname()).isNotEmpty();
		log.info("Twitter to salesforce integration test finished.");
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

	private void deleteSalesforceContact(ForceApi salesforce, String screenName) {
		final Optional<Contact> contact = getSalesforceContact(salesforce, screenName);
		if (contact.isPresent()) {
			final String id = String.valueOf(contact.get().getId());
			salesforce.deleteSObject("contact", id);
			log.info("Deleting salesforce contact: {}", contact.get());
		}
	}

	private Optional<Contact> getSalesforceContact(ForceApi salesforce, String twitterName) {
		final QueryResult<Contact> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Description,Title FROM contact where Title='"
				+ twitterName + "'", Contact.class);
		final Optional<Contact> contact = queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
		return contact;
	}
}
